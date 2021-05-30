cd server
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar spigot.jar nogui
 https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar