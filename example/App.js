/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {useEffect} from 'react';
import {
  SafeAreaView,
  StyleSheet,
  ScrollView,
  View,
  Text,
  StatusBar,
    TouchableHighlight,
} from 'react-native';

import MSAL from 'react-native-msal-module';

import {
  Header,
  LearnMoreLinks,
  Colors,
  DebugInstructions,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

const CLIENT_ID = 'e760cab2-b9a1-4c0d-86fb-ff7084abd902';
const scopes = ['https://fabrikamb2c.onmicrosoft.com/helloapi/demo.read'];

const App: () => React$Node = () => {
  useEffect(() => {
    MSAL.init(CLIENT_ID);
  }, []);
  const login = async () => {
    try {
      const resposne = await MSAL.acquireTokenAsync(scopes);
      console.log(resposne);
    } catch (e) {
      console.log(e);
    }
  }
  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView>
        <ScrollView
          contentInsetAdjustmentBehavior="automatic"
          style={styles.scrollView}>
          <Header />
          <View>
            <TouchableHighlight onPress={login}>
              <Text>LOGIN</Text>
            </TouchableHighlight>
          </View>
        </ScrollView>
      </SafeAreaView>
    </>
  );
};

const styles = StyleSheet.create({
  scrollView: {
    backgroundColor: Colors.lighter,
  },
  engine: {
    position: 'absolute',
    right: 0,
  },
  body: {
    backgroundColor: Colors.white,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
    color: Colors.black,
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
    color: Colors.dark,
  },
  highlight: {
    fontWeight: '700',
  },
  footer: {
    color: Colors.dark,
    fontSize: 12,
    fontWeight: '600',
    padding: 4,
    paddingRight: 12,
    textAlign: 'right',
  },
});

export default App;
