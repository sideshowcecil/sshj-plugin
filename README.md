# sshj-plugin Rundeck Plugin

This is a node executor / File Copier plugin based on [SSHJ](https://github.com/hierynomus/sshj) library

## Build and Install

```
./gradlew clean && ./gradlew build 

cp build/libs/sshj-plugin-x.x.x.jar $RDECK_BASE/libext
```

## How to use

This plugin has the same configuration names than the default JSCH plugin, so most of this [documentation](https://docs.rundeck.com/docs/administration/projects/node-execution/ssh.html)  works for this plugin.

### Set it at project level

Go to `Project Settings > Edit Configuration` to set the SSHJ plugin at project level

* Node Executor

![Node Executor](docs/project-node-executor.png)

* File Copier

![Node Executor](docs/project-file-copier.png)

* Settings

![Node Executor](docs/node-executor-settings.png)

### Set at node level

Use `node-executor` and `file-copier` node attributes to use SSHJ plugin at node level.

```
Demo-Ubuntu:
  nodename: Demo-Ubuntu
  hostname: 192.168.100.18
  description: Ubuntu 20
  username: samuel
  osFamily: unix
  node-executor: sshj-ssh
  file-copier: sshj-scp
  tags: ubuntu
  ssh-authentication: password
  ssh-password-storage-path: keys/node/samuel.password
  sudo-password-storage-path: keys/node/sudo.password
  ssh-password-option: option.password
  sudo-command-enabled: 'true'
  
```

### Host key verification

By default the plugin accepts any host key presented by the node. To only connect to nodes whose host key you trust, enable strict host key checking:

| Node attribute | Project property | Framework property | Description |
|---|---|---|---|
| `ssh-strict-host-key-checking` | `project.ssh-strict-host-key-checking` | `framework.ssh-strict-host-key-checking` | `true` to require the node's host key to be in the known hosts file. Unknown hosts and changed host keys fail the connection. Default `false`. |
| `ssh-known-hosts-file` | `project.ssh-known-hosts-file` | `framework.ssh-known-hosts-file` | Path to an OpenSSH `known_hosts` file on the Rundeck server. Default: `~/.ssh/known_hosts` and `~/.ssh/known_hosts2` of the user running Rundeck. |

Both can also be set in the plugin settings at project or framework level. The known hosts file uses the standard OpenSSH format, so it can be built with `ssh-keyscan` as the user running Rundeck (check the fingerprints against a trusted source):

```
ssh-keyscan -H node1.example.com >> ~/.ssh/known_hosts
```

Nodes on a non-default port are matched as `[host]:port`, the same as OpenSSH.

# Status

- [X] Key Storage Password Authentication
- [x] File Key Authentication 
- [x] Key Storage Key Authentication (it works with a creating a temp file from key storage, TODO: https://github.com/hierynomus/sshj/issues/350#issuecomment-336457625)
- [x] Passphrase 
- [x] Env Variables 
- [X] Sudo Commands (Experimental, works with [Java ExpectIt lib](https://github.com/Alexey1Gavrilov/ExpectIt))
- [X] Username from input option
- [X] Password from input option
- [X] Sudo Password from input option
- [X] Passphrase from input option
- [X] Keep alive
- [X] File Transfer Single File
- [ ] File Transfer Multiples File
