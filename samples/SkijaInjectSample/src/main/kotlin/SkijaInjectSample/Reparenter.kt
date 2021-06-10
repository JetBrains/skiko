package SkijaInjectSample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.ColorType
import org.jetbrains.skiko.*
import java.awt.*
import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.*
import java.net.InetAddress.getByName
import java.net.ServerSocket
import java.net.Socket
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.awt.FlowLayout

private fun OutputStream.sendCommand(command: String) {
    val data = command.toByteArray()
    System.err.println("send ${data.size}")
    // TODO: lift if needed
    assert(data.size < 128)
    this.write(data.size)
    this.write(data)
    this.flush()
}

private fun InputStream.receiveCommand(): String? {
    return try {
        val size = this.read()
        if (size == -1) {
            null
        } else {
            assert(size < 128)
            val bytes = ByteArray(size)
            this.read(bytes)
            System.err.println("${ProcessHandle.current().pid()} got $size: ${bytes.toString(Charsets.UTF_8)}")
            bytes.toString(Charsets.UTF_8)
        }
    } catch (e: IOException) {
        null
    }
}


class RemoterServer(val onClose: () -> Unit, val processor: (String, InputStream, OutputStream) -> String) {
    val serverSocket = ServerSocket(0, 0, getByName("127.0.0.1")).apply {
        reuseAddress = true
    }
    val port = serverSocket.localPort
    init {
        thread {
            while (true) {
                val socket = serverSocket.accept()
                val inputStream = socket.getInputStream()
                val outputStream = socket.getOutputStream()
                try {
                    do {
                        val line = inputStream.receiveCommand()
                        if (line != null) {
                            println("SERVER: $line")
                            val result = processor(line, inputStream, outputStream)
                            println("SERVER response: $result")
                            outputStream.sendCommand(result)
                        }
                    } while (line != null)
                    System.err.println("server connection closed")
                } catch (e: IOException) {}
                inputStream.close()
                outputStream.close()
                socket.close()
                serverSocket.close()
                onClose()
            }
        }
    }
}

class RemoterClient(port: Int) {
    private val socket = Socket("localhost", port)
    val input = socket.getInputStream()
    val output = socket.getOutputStream()

    @Synchronized
    fun sendCommand(command: String, onClose: () -> Unit, onResult: (String) -> Unit) {
        System.err.println("CLIENT: SEND $command")
        output.sendCommand(command)
        val line = input.receiveCommand()
        if (line != null) {
            System.err.println("CLIENT: GOT $line")
            onResult(line)
        } else {
            System.err.println("CLIENT: CLOSED")
            input.close()
            output.close()
            onClose()
        }
    }
}

fun mainReparent(kind: String) {
    if (kind == "server") {
        mainReparentServer()
    } else {
        mainReparentClient(kind.split("_")[1].toInt())
    }
}

fun mainReparentServer() {
    val frame = JFrame("Reparent Server")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.preferredSize = Dimension(1000, 600)

    // It's critical that we use AWT Canvas to get Window handle, our APIs only work for such case.
    val canvas = Canvas()
    canvas.size = Dimension(900, 600)
    val g2 = BufferedImage(200, 200, TYPE_INT_RGB).createGraphics()
    g2.drawString("ZZZZ", 10, 10)
    canvas.printAll(g2)
    frame.contentPane.add(canvas)
    canvas.isVisible = true

    frame.pack()
    frame.isVisible = true
    frame.layout = FlowLayout()

    println("Start server ${canvas.windowNumber}")
    var frameData = ByteArray(1)
    val server = RemoterServer(onClose = {
        println("Close server")
        exitProcess(0)
    } ) { command, input, output ->
        val words = command.split(' ')
        if (words.isNotEmpty()) {
            when (words[0]) {
                "ATTACH" -> {
                    "${ProcessHandle.current().pid()} ${canvas.windowNumber} ${frame.location.x} ${frame.location.y} 70 50 300 100"
                }
                "FRAME" -> {
                    val width = words[1].toInt()
                    val height = words[2].toInt()
                    val size = words[3].toInt()

                    // TODO: reuse
                    println("SERVER: READING FRAME OF $size")
                    if (frameData.size != size)
                        frameData = ByteArray(size)
                    input.read(frameData)
                    println("SERVER: GOT FRAME")
                    val raster = Raster.createInterleavedRaster(
                            DataBufferByte(frameData, frameData.size),
                            width,
                            height,
                            width * 4,
                            4,
                            intArrayOf(2, 1, 0, 3),
                            null
                    )
                    val colorModel = ComponentColorModel(
                            ColorSpace.getInstance(ColorSpace.CS_sRGB),
                            true,
                            false,
                            Transparency.TRANSLUCENT,
                            DataBuffer.TYPE_BYTE
                    )
                    val image = BufferedImage(colorModel, raster!!, false, null)
                    println("created $image")
                    SwingUtilities.invokeAndWait {
                        println("DRAW!")
                        //val g2 = image.createGraphics()
                        val g2 = BufferedImage(width, height, TYPE_INT_RGB).createGraphics()
                        g2.drawString("XXXXX", 10, 10)
                        canvas.printAll(g2)
                        g2.dispose()
                        frame.repaint()
                    }
                    "OK"
                }
                else -> {
                    throw Error("Unknown command: '${words[0]}'")
                }
            }
        } else {
            "empty command"
        }
    }
    thread {
        val jvm = "${System.getProperty("java.home")}/bin/java"
        val cp = System.getProperty("java.class.path")
        val proc = ProcessBuilder(jvm, "-Dskiko.reparent=client_${server.port}", "-cp", cp, "SkijaInjectSample.AppKt")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        proc.errorStream.bufferedReader().useLines {
            it.forEach { println("CHILD ERR: $it") }
        }
        proc.inputStream.bufferedReader().useLines {
            it.forEach { println("CHILD OUT: $it") }
        }
    }
}

fun mainReparentClient(port: Int) = runBlocking(Dispatchers.Swing) {
    val remoter = RemoterClient(port)
    val skiaWindow = SkiaWindow(SkiaLayerProperties(isInvisible = true))
    skiaWindow.apply {
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        title = "Reparent Client"
        isUndecorated = true
        //isAlwaysOnTop = true
        preferredSize = Dimension(400, 200)
        location = Point(300, 300)
        pack()
        layer.awaitRedraw()
        //isVisible = true
        layer.renderer = Renderer(layer) { renderer, w, h, nanoTime ->
            val canvas = renderer.canvas!!
            canvas.drawString("$nanoTime", (w/3).toFloat(), (h/2).toFloat(), renderer.font, renderer.paint)
        }
    }

    thread {
        remoter.sendCommand("ATTACH", onClose = {
            System.err.println("Closing client")
            exitProcess(0)
        }) { response ->
            val words = response.split(" ")
            val pid = words[0].toLong()
            val winId = words[1].toLong()
            val x_global = words[2].toInt()
            val y_global = words[3].toInt()
            val x_relative = words[4].toInt()
            val y_relative = words[5].toInt()
            val w = words[6].toInt()
            val h = words[7].toInt()
            System.err.println("would reparent to $pid $winId")
            SwingUtilities.invokeAndWait() {
                //skiaWindow.location = Point(x_global + x_relative, y_global + y_relative)
                skiaWindow.size = Dimension(w, h)
                //skiaWindow.reparentTo(pid, winId, x_relative, y_relative, w, h)
            }
        }

        // Frame stream.
        while (true) {
            val bitmap = skiaWindow.layer.screenshot()
            if (bitmap != null) {
                assert(bitmap.colorInfo.colorType == ColorType.BGRA_8888)
                val pixels = bitmap.readPixels()!!
                val command = "FRAME ${bitmap.width} ${bitmap.height} ${pixels.size}"
                System.err.println("CLIENT: SEND $command")
                remoter.output.sendCommand(command)
                remoter.output.write(pixels)
                remoter.output.flush()
                System.err.println("CLIENT: SENT FRAME")
                val line = remoter.input.receiveCommand()
                if (line != null) {
                    System.err.println("CLIENT: GOT $line")
                } else {
                    System.err.println("CLIENT: CLOSED")
                    remoter.input.close()
                    remoter.output.close()
                    exitProcess(0)
                }
            }
            Thread.sleep(1000)
        }
    }
}