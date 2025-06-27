// This file is merged with skiko.js by emcc

export const { _callCallback, _registerCallback, _releaseCallback, _createLocalCallbackScope, _releaseLocalCallbackScope } = SkikoCallbacks;

const skikoModule = new Promise((resolve, reject) => {
   (async function() {
      const skikoModule = await loadSkikoWASM();
      resolve(skikoModule)
   }())
});


export function onWasmReady(onReady) {
   console.log("onWasmReady");
    skikoModule.then(onReady)
}
