import admin from "firebase-admin";
import dotenv from "dotenv";

dotenv.config();

admin.initializeApp({
    credential: admin.credential.cert(JSON.parse(process.env.FIREBASE_CREDENTIALS as string))
});

export const sendNotification = async (token: string, title: string, body: string) => {
    const message = {
        notification: { title, body },
        token
    };

    try {
        await admin.messaging().send(message);
        console.log("Notification sent successfully");
    } catch (error) {
        console.error("Error sending notification:", error);
    }
};
