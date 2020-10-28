# sshj-plugin Rundeck Plugin

This is a node executor / File Copier plugin based on SSHJ library

## Build and Install

```
./gradlew clean && ./gradlew build 

cp build/libs/sshj-plugin-x.x.x.jar $RDECK_BASE/libext
```

## How to use

### Project Level


### Node Level 



# Status

- [X] Key Storage Password Authentication
- [x] File Key Authentication 
- [x] Key Storage Key Authentication (I just created a temp file fromkey storage, TODO: https://github.com/hierynomus/sshj/issues/350#issuecomment-336457625)
- [x] Passphrase 
- [x] Env Variables 
- [ ] Sudo Commands
- [X] Username from input option
- [X] Password from input option
- [ ] Sudo Password from input option
- [ ] Passphrase from input option
- [X] Keep alive
- [X] File Transfer Single File
- [ ] File Transfer Multiples File
