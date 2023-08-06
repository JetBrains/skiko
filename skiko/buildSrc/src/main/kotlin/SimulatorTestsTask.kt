import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

open class SimulatorTestsTask: DefaultTask() {

    @InputFile
    val testExecutable = project.objects.fileProperty()

    @Input
    val simulatorId = project.objects.property(String::class.java)

    @TaskAction
    fun runTests() {
        val device = simulatorId.get()
        val bootResult = project.exec { commandLine("xcrun", "simctl", "boot", device) }
        try {
            println(device)
            print(testExecutable.get())
            val spawnResult = project.exec { commandLine("xcrun", "simctl", "spawn", device, testExecutable.get()) }
            spawnResult.assertNormalExitValue()

        } finally {
            if (bootResult.exitValue == 0) {
                project.exec { commandLine("xcrun", "simctl", "shutdown", device) }
            }
        }
    }
}
