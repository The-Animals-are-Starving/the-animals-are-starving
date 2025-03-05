import admin, { ServiceAccount } from "firebase-admin";
import { cert } from "firebase-admin/app";
require('dotenv').config();

const firebaseServiceAccount = process.env.FIREBASE_SERVICE_ACCOUNT;
if (!firebaseServiceAccount) {
  throw new Error("FIREBASE_SERVICE_ACCOUNT is not defined in the environment variables.");
}

const serviceAccountKey: ServiceAccount = JSON.parse(firebaseServiceAccount);

admin.initializeApp({
  credential: cert(serviceAccountKey),
});

export default admin;
