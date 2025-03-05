import admin, { ServiceAccount } from "firebase-admin";
import { initializeApp, cert } from "firebase-admin/app";
const serviceAccountKey = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);

const serviceAccountKey: ServiceAccount = {
  projectId: serviceAccount.project_id,
  privateKey: serviceAccount.private_key,
  clientEmail: serviceAccount.client_email,
};

admin.initializeApp({
  credential: cert(serviceAccountKey),
});

export default admin;
