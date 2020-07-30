// drawcanvas.cpp : Defines the exported functions for the DLL application.

#include "drawcanvas.h"
#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <unistd.h>
#include <stdio.h>
#include <set>

using namespace std;

typedef GLXContext (*glXCreateContextAttribsARBProc)(Display *, GLXFBConfig, GLXContext, Bool, const int *);

JavaVM *jvm = NULL;

class LayersSet
{
public:
	jobject windowRef;
	GLXContext context;
	Display *display;
	Drawable glLayer;

	void update()
	{
		draw();
	}

	void dispose()
	{
		glLayer = 0;
		context = NULL;
		display = NULL;
	}

private:
	bool isDrawing = false;
	void draw()
	{
		if (isDrawing)
		{
			return;
		}

		isDrawing = true;
		glXMakeCurrent(display, glLayer, context);

		if (jvm != NULL)
		{
			JNIEnv *env;
			jvm->AttachCurrentThread((void **)&env, NULL);

			jclass wndClass = env->GetObjectClass(windowRef);
			jmethodID drawMethod = env->GetMethodID(wndClass, "draw", "()V");
			if (NULL == drawMethod)
			{
				fprintf(stderr, "The method Window.draw() not found!\n");
				return;
			}
			env->CallVoidMethod(windowRef, drawMethod);
		}

		glXMakeCurrent(display, 0, 0);
		isDrawing = false;
	}
};

set<LayersSet *> *windowsSet = NULL;
LayersSet *findByObject(JNIEnv *env, jobject object)
{
	for (auto &layer : *windowsSet)
	{
		if (env->IsSameObject(object, layer->windowRef) == JNI_TRUE)
		{
			return layer;
		}
	}
	fprintf(stderr, "The set does not contain this window.\n");
	return NULL;
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_Components_Window_updateLayer(JNIEnv *env, jobject window)
{
	if (windowsSet != NULL)
	{
		LayersSet *layer = findByObject(env, window);
		if (layer != NULL)
		{
			if (layer->display == NULL && layer->context == NULL)
			{
				env->DeleteGlobalRef(layer->windowRef);
				layer->windowRef = NULL;
				windowsSet->erase(layer);
				delete layer;
			}
			return;
		}
	}
	else
	{
		windowsSet = new set<LayersSet *>();
	}

	JAWT awt;
	JAWT_DrawingSurface *ds = NULL;
	JAWT_DrawingSurfaceInfo *dsi = NULL;

	jboolean result = JNI_FALSE;
	jint lock = 0;
	JAWT_X11DrawingSurfaceInfo *dsi_x11;

	awt.version = (jint)JAWT_VERSION_9;
	result = JAWT_GetAWT(env, &awt);

	if (result == JNI_FALSE)
	{
		fprintf(stderr, "JAWT_GetAWT failed! Result is JNI_FALSE\n");
		return;
	}

	if (jvm == NULL)
	{
		env->GetJavaVM(&jvm);
	}

	ds = awt.GetDrawingSurface(env, window);

	lock = ds->Lock(ds);

	dsi = ds->GetDrawingSurfaceInfo(ds);

	// Get the platform-specific drawing info
	dsi_x11 = (JAWT_X11DrawingSurfaceInfo *)dsi->platformInfo;
	Display *display = dsi_x11->display;

	if (dsi != NULL)
	{
		static int pfa[] = {
			GLX_RENDER_TYPE, GLX_RGBA_BIT,
			GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT,
			GLX_DOUBLEBUFFER, true,
			GLX_RED_SIZE, 1,
			GLX_GREEN_SIZE, 1,
			GLX_BLUE_SIZE, 1,
			None};

		int num_fbc = 0;
		GLXFBConfig *fbc = glXChooseFBConfig(display, DefaultScreen(display), pfa, &num_fbc);
		if (!fbc)
		{
			printf("glXChooseFBConfig() failed\n");
			return;
		}

		XVisualInfo *vi = glXGetVisualFromFBConfig(display, fbc[0]);
		GLXContext ctx_old = glXCreateContext(display, vi, 0, GL_TRUE);
		glXCreateContextAttribsARBProc glXCreateContextAttribsARB = 0;
		glXCreateContextAttribsARB = (glXCreateContextAttribsARBProc)
			glXGetProcAddress((const GLubyte *)"glXCreateContextAttribsARB");
		static int context_attribs[] = {
			GLX_CONTEXT_MAJOR_VERSION_ARB, 4,
			GLX_CONTEXT_MINOR_VERSION_ARB, 2,
			None};

		GLXContext context = glXCreateContextAttribsARB(display, fbc[0], NULL, true, context_attribs);
		if (!context)
		{
			printf("Failed to create OpenGL context. Exiting.\n");
			return;
		}

		LayersSet *layer = new LayersSet();
		windowsSet->insert(layer);

		jobject windowRef = env->NewGlobalRef(window);

		layer->windowRef = windowRef;
		layer->display = display;
		layer->glLayer = dsi_x11->drawable;
		layer->context = context;
	}

	ds->FreeDrawingSurfaceInfo(dsi);

	ds->Unlock(ds);

	awt.FreeDrawingSurface(ds);
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_Components_Window_redrawLayer(JNIEnv *env, jobject window)
{
	LayersSet *layer = findByObject(env, window);
	if (layer != NULL)
	{
		layer->update();
	}
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_Components_Window_disposeLayer(JNIEnv *env, jobject window)
{
	LayersSet *layer = findByObject(env, window);
	if (layer != NULL)
	{
		layer->dispose();
	}
}

JNIEXPORT jfloat JNICALL Java_org_jetbrains_awthrl_Components_Window_getContentScale(JNIEnv *env, jobject window)
{
	LayersSet *layer = findByObject(env, window);
	if (layer != NULL)
	{
		// get scale dpi factor of current monitor
	}
	return 1.0f;
}