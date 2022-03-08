import shutil
import subprocess

pluginPath = "out/artifacts/Sharehealth/Sharehealth.jar"

# local server
shutil.copy2(pluginPath, "server/plugins")

# remote server
subprocess.run(["scp", pluginPath, "minecraft@130.61.179.201:/home/minecraft/servers/flattest/plugins"])

