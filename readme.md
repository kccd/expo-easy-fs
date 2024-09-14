expo-easy-fs
---
A simple file operation tool that supports only the Android platform. Used to supplement the missing features in [expo-file-system](https://www.npmjs.com/package/expo-file-system).

## Install
```
npm install expo-easy-fs
// or 
yarn add expo-easy-fs
```

## API
### copyFileToDownload
Moves a file to the system's download directory.

```typescript
const sourceFileUri = 'path/to/the/file.zip';
const destinationFilename = 'file.zip';
await copyFileToDownload(sourceFileUri, destinationFilename);
```