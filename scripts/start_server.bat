cd server
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar paper.jar nogui