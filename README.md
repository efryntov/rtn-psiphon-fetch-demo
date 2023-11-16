## Demo app build instructions
```
# add the RTNPsiphonFetch module 
cd PsiphonFetchDemoApp/
yarn add ../RTNPsiphonFetch

# run Codegen
cd android
./gradlew generateCodegenArtifactsFromSchema

# run the app!
cd ..
yarn android

```