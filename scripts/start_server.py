import os
import subprocess

p = subprocess.Popen(
    "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar paper.jar nogui",
    cwd=os.getcwd() + "/server", shell=True, stdin=subprocess.PIPE
)


#os.system("cd server && java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar paper.jar nogui")
