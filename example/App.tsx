import * as ExpoEasyFs from "expo-easy-fs";
import * as fileSystem from "expo-file-system";
import { StyleSheet, Text, View, StatusBar, Button } from "react-native";
import {exists, getPaths, mkdir, remove} from "expo-easy-fs";

export default function App() {
  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <Text>Hello world</Text>
      <Button
        title="Copy file to download directory"
        onPress={() => {
          const filename = `expo-easy-fs_${Date.now()}.txt`;
          const filePath = fileSystem.documentDirectory + filename;
          fileSystem
            .writeAsStringAsync(filePath, filename)
            .then(() => {
              return ExpoEasyFs.copyFileToDownload(filePath, filename);
            })
            .then(() => {
              alert("Copied!");
            })
            .catch((err) => {
              alert(err.message);
            });
        }}
      />
      <Button title={'mkdir'} onPress={() => {
        getPaths().then(paths => {
          const {downloads} = paths;
          return mkdir(`${downloads}/KeChuangTest/aaa/bbb`)
        })
          .then((r) => {
            console.log(`mkdir done: ${r}`);
          }).catch(console.error);
      }} />

      <Button title={'mkdir & remove'} onPress={async () => {
        try{
          const paths = await getPaths();
          const targetPath = `${paths.downloads}/KeChuangTest/111/222`;
          await mkdir(targetPath);
          console.log(`mkdir done: ${targetPath}`);
          let isExists = await exists(targetPath);
          console.log("exists: ", isExists);
          await remove(targetPath);
          console.log(`remove done: ${targetPath}`);
          isExists = await exists(targetPath);
          console.log("exists: ", isExists);
        } catch(err) {
          console.error(err);
        }
      }} />
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
