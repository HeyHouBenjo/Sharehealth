cd server || exit
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar spigot.jar nogui