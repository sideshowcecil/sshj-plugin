## SSHJ test

### Copy plugin before starts
```
./gradlew build
cp build/libs/sshj-plugin-0.1.X-SNAPSHOT.jar docker/rundeck/plugins
```

### **Build and Up**  
> **NOTE:** Building is not necessary the first time. Use 'build' to update the images used to the latest version.
```
docker-compose build
docker-compose up -d
```