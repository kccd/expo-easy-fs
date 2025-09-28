# expo-easy-fs

[![npm downloads](https://img.shields.io/npm/dm/expo-easy-fs.svg)](https://www.npmjs.com/package/expo-easy-fs)
[![license](https://img.shields.io/npm/l/expo-easy-fs.svg)](https://github.com/kccd/expo-easy-fs/blob/main/LICENSE)
[![platform](https://img.shields.io/badge/platform-Expo-blue.svg)](https://expo.dev/)
[![support](https://img.shields.io/badge/support-Android-green.svg)](https://expo.dev/)

A file utility for Expo to access the system Downloads folder, Android-only.

## Android 权限与兼容性 (Permissions & Compatibility)

从 vX.X.X 版本开始(包含此修改)，`expo-easy-fs` 适配 Android 各版本存储策略：

| Android 版本 | 存储模型 | 需要的权限 / 配置 | 说明 |
|--------------|----------|------------------|------|
| API < 29 (Android 9 及以下) | Legacy 外部存储 | 必须在 `AndroidManifest.xml` 中声明 `READ_EXTERNAL_STORAGE` 与 `WRITE_EXTERNAL_STORAGE`，并在运行时申请（WRITE 为必需）。| 直接写入公共 `Downloads` 目录，需要用户授予权限。|
| API 29 (Android 10) | Scoped Storage (过渡) | 无需 WRITE 权限；若要保持旧行为可加 `requestLegacyExternalStorage=true`（不推荐）。| 本库使用 MediaStore 写入 Downloads 集合，无需额外权限。|
| API 30+ (Android 11 及以上) | Scoped Storage | 无需 `WRITE_EXTERNAL_STORAGE`；普通下载不需要 `MANAGE_EXTERNAL_STORAGE`。| 使用 MediaStore，系统自动管理。|

### 需要在 Expo / React Native 项目中配置的 Manifest 权限

如果你仍需要兼容 Android 9 及以下，请在 `app.json` 或 `app.config.js` 中添加：

```jsonc
"android": {
  "permissions": [
    "READ_EXTERNAL_STORAGE",
    "WRITE_EXTERNAL_STORAGE"
  ]
}
```

并在运行时（仅 API <29）调用：

```ts
import { PermissionsAndroid, Platform } from 'react-native';

async function ensureLegacyStoragePermission() {
  if (Platform.OS === 'android' && Platform.Version < 29) {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
      {
        title: '存储权限',
        message: '需要存储权限以便保存文件到下载目录',
        buttonPositive: '确定'
      }
    );
    if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
      throw new Error('WRITE_EXTERNAL_STORAGE denied');
    }
  }
}
```

> 提示：Android 13 (API 33) 引入了细化的媒体权限（如 `READ_MEDIA_IMAGES` 等），由于本库写入的是通用文件（可能是任意类型），通常无需这些媒体读取权限；下载后若你再读取/处理特定媒体类型，按需自行申请。

### 使用注意 (Notes)

1. 在 Android 10+ 上，本库通过 MediaStore 写入公共 Downloads，不需要手动申请写权限。
2. 旧方法 `copyFileToDownload` 现在接受 `content://`、`file://` 或绝对路径，内部自动适配。
3. 如果你使用的是 Expo Managed Workflow，请确保构建时所需的权限都在配置里列出，否则旧设备上会失败。
4. 不要为仅保存普通下载文件去申请 `MANAGE_EXTERNAL_STORAGE`（All files access），那会触发应用上架审核风险。
5. 若你的源文件来自 `expo-file-system`，传入前可用本库的 `fixPath` 处理前缀。

### 新增/更新行为 (Changelog 摘要)

- 增强 `copyFileToDownload`：支持 Android 10+ Scoped Storage (MediaStore) 与旧版直写逻辑。
- 自动判断输入是 `content://`、`file://` 还是普通路径。
- 根据文件扩展名推断 MIME Type，提升在系统文件管理器中的可见性。
- 对 API <29 若无写权限会直接抛出 `ERR_PERMISSION_DENIED`。

### 最低示例（含权限处理）

```ts
import * as ExpoEasyFs from 'expo-easy-fs';
import * as FileSystem from 'expo-file-system';

async function demo() {
  await ensureLegacyStoragePermission(); // 仅旧设备需要
  const filename = 'demo.txt';
  const localPath = FileSystem.documentDirectory + filename;
  await FileSystem.writeAsStringAsync(localPath, 'Hello');
  const { downloads } = await ExpoEasyFs.getPaths();
  await ExpoEasyFs.copyFile(localPath, `${downloads}/demo-folder/${filename}`);
}
```

---


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