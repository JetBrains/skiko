package SkijaInjectSample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.SkiaWindow
import org.jetbrains.skiko.windowNumber
import java.awt.Canvas
import java.awt.Dimension
import java.awt.Point
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.InetAddress.getByName
import java.net.ServerSocket
import java.net.Socket
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class RemoterServer(val onClose: () -> Unit, val processor: (String) -> String) {
    val serverSocket = ServerSocket(0, 0, getByName("127.0.0.1")).apply {
        reuseAddress = true
    }
    val port = serverSocket.localPort
    init {
        thread {
            while (true) {
                val socket = serverSocket.accept()
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = PrintStream(socket.getOutputStream())
                do {
                    val line = reader.readLine()
                    if (line != null) {
                        var result = ""
                        println("SERVER: $line")
                        SwingUtilities.invokeAndWait {
                            result = processor(line)
                        }
                        println("SERVER response: $result")
                        writer.println(result)
                    }
                } while (line != null)
                reader.close()
                writer.close()
                socket.close()
                serverSocket.close()
                onClose()
            }
        }
    }
}

class RemoterClient(port: Int) {
    private val socket = Socket("localhost", port)
    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    val writer = PrintStream(socket.getOutputStream())

    @Synchronized
    fun sendCommand(command: String, onClose: () -> Unit, onResult: (String) -> Unit) {
        println("CLIENT: SEND $command")
        writer.println(command)
        writer.flush()
        val line = reader.readLine()
        if (line != null) {
            println("CLIENT: GOT $line")
            onResult(line)
        } else {
            println("CLIENT: CLOSED")
            writer.close()
            reader.close()
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
    frame.preferredSize = Dimension(400, 200)

    val panel = Canvas()
    panel.size = Dimension(200, 100)
    frame.contentPane.add(panel)

    frame.pack()
    panel.repaint()

    frame.isVisible = true

    println("Start server ${panel.windowNumber}")
    val server = RemoterServer(onClose = {
        println("Close server")
        exitProcess(0)
    } ) { command ->
        val words = command.split(' ')
        if (words.isNotEmpty()) {
            when (words[0]) {
                "ATTACH" -> {
                    "${ProcessHandle.current().pid()} ${panel.windowNumber} ${frame.location.x} ${frame.location.y + 25} ${panel.width} ${panel.height}"
                }
                "PING" -> {
                    "OK"
                }
                else -> {
                    "Unknown command: ${words[0]}"
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
    val skiaWindow = SkiaWindow()
    skiaWindow.apply {
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        title = "Reparent Client"
        isUndecorated = true
        isAlwaysOnTop = true
        preferredSize = Dimension(400, 200)
        location = Point(300, 300)
        pack()
        layer.awaitRedraw()
        isVisible = true
        layer.renderer = Renderer(layer) { renderer, w, h, nanoTime ->
            val canvas = renderer.canvas!!
            canvas.drawString("$nanoTime", (w/3).toFloat(), (h/2).toFloat(), renderer.font, renderer.paint)
        }
    }

    remoter.sendCommand("ATTACH", onClose = {
        println("Closing client")
        exitProcess(0)
    }) { response ->
        val words = response.split(" ")
        val pid = words[0].toLong()
        val winId = words[1].toLong()
        val x = words[2].toInt()
        val y = words[3].toInt()
        val w = words[4].toInt()
        val h = words[5].toInt()
        println("would reparent to $pid $winId")
        SwingUtilities.invokeLater {
            skiaWindow.location = Point(x, y)
            skiaWindow.size = Dimension(w, h)
            skiaWindow.reparentTo(pid, winId)
        }
    }
    thread {
        // Keepalive.
        while (true) {
            remoter.sendCommand("PING", onClose = {
                println("Closing client")
                exitProcess(0)
            }) { _ -> }
            Thread.sleep(1000)
        }
    }
}