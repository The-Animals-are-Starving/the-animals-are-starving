import admin, { ServiceAccount } from "firebase-admin";
import { cert } from "firebase-admin/app";
require('dotenv').config();

const { FIREBASE_PROJECT_ID, FIREBASE_PRIVATE_KEY, FIREBASE_CLIENT_EMAIL } = process.env;

if (!FIREBASE_PROJECT_ID || !FIREBASE_PRIVATE_KEY || !FIREBASE_CLIENT_EMAIL) {
  throw new Error("Missing Firebase configuration in environment variables.");
}

const serviceAccount: ServiceAccount = {
  projectId: FIREBASE_PROJECT_ID,
  privateKey: FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
  clientEmail: FIREBASE_CLIENT_EMAIL,
};

admin.initializeApp({
  credential: cert(serviceAccount),
});

export default admin;
