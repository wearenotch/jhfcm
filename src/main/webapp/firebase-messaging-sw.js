importScripts('https://www.gstatic.com/firebasejs/8.6.1/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.6.1/firebase-messaging.js');

const firebaseConfig = {
    apiKey: "AIzaSyDSt-ls4ETYEzIOh1ZGEuo7387PIJp_8E8",
    authDomain: "jh-lab-5417a.firebaseapp.com",
    projectId: "jh-lab-5417a",
    storageBucket: "jh-lab-5417a.appspot.com",
    messagingSenderId: "544462390526",
    appId: "1:544462390526:web:9cce3bcbd66c7db395d1af"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();
