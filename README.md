# expo-easy-fs

[![npm downloads](https://img.shields.io/npm/dm/expo-easy-fs.svg)](https://www.npmjs.com/package/expo-easy-fs)
[![license](https://img.shields.io/npm/l/expo-easy-fs.svg)](https://github.com/kccd/expo-easy-fs/blob/main/LICENSE)
[![platform](https://img.shields.io/badge/platform-Expo-blue.svg)](https://expo.dev/)
[![support](https://img.shields.io/badge/support-Android-green.svg)](https://expo.dev/)

A file utility for Expo to access the system Downloads folder, Android-only.

### Installation
```bash
npm install expo-easy-fs
```

### API
```typescript
/**
 * Retrieves system paths such as the downloads directory.
 * @returns {Promise<{ downloads: string }>} A promise resolving to an object containing the downloads path.
 */
export function getPaths(): Promise<{
  downloads: string;
}> {
  return ExpoEasyFsModule.getPaths();
}

/**
 * Creates a new directory.
 * @param {string} dirPath - The path of the directory to create.
 * @returns {Promise<string>} A promise resolving to the created directory's path.
 */
export function mkdir(dirPath: string): Promise<string> {
  return ExpoEasyFsModule.mkdir(fixPath(dirPath));
}

/**
 * Copies a file from the source path to the destination path.
 * @param {string} sourcePath - The source file path.
 * @param {string} destinationPath - The destination file path.
 * @returns {Promise<string>} A promise resolving to the destination file's path.
 */
export function copyFile(sourcePath: string, destinationPath: string): Promise<string> {
  return ExpoEasyFsModule.copyFile(fixPath(sourcePath), fixPath(destinationPath));
}

/**
 * Deletes a file or directory.
 * @param {string} targetPath - The path of the file or directory to delete.
 * @returns {Promise<string>} A promise resolving to the deleted target's path.
 */
export function remove(targetPath: string): Promise<string> {
  return ExpoEasyFsModule.remove(fixPath(targetPath));
}

/**
 * Checks if a path exists and returns its type.
 * @param {string} targetPath - The path to check.
 * @returns {Promise<{ exists: boolean, type: 'file' | 'directory' | 'unknown', path: string }>} 
 * A promise resolving to an object indicating existence, type, and the path.
 */
export function exists(targetPath: string): Promise<{
  exists: boolean;
  type: 'file' | 'directory' | 'unknown';
  path: string;
}> {
  return ExpoEasyFsModule.exists(fixPath(targetPath));
}
```

### Example
```javascript
import * as ExpoEasyFs from "expo-easy-fs";
import * as fileSystem from "expo-file-system";
import { StyleSheet, Text, View, StatusBar, Button } from "react-native";
import { useCallback } from "react";

export default function App() {
  const func = useCallback(async () => {
    try {
      const filename = `expo-easy-fs-${Date.now()}.txt`;
      const originFilePath = fileSystem.documentDirectory + filename;

      const { downloads: systemDownloadsDir } = await ExpoEasyFs.getPaths();

      const destinationDirPath = systemDownloadsDir + `/expo-easy-fs/`;
      const destinationFilePath = destinationDirPath + filename;

      await fileSystem.writeAsStringAsync(originFilePath, "Test");
      await ExpoEasyFs.mkdir(destinationDirPath);
      await ExpoEasyFs.copyFile(originFilePath.replace(/^file:\//, ""), destinationFilePath);
      await ExpoEasyFs.remove(originFilePath);
      alert(`Success! The test file has been created and moved to: ${destinationFilePath}`);
    } catch (err) {
      console.error(err);
      alert("An error occurred while processing the file. Please try again.");
    }
  }, []);

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <Text>Welcome to the Expo Easy FS Demo</Text>
      <Button
        title="Create a file and move it to the system's Downloads folder"
        onPress={func}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
  },
});

```