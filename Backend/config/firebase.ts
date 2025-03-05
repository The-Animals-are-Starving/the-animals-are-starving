import admin, { ServiceAccount } from "firebase-admin";
import { initializeApp, cert } from "firebase-admin/app";
import serviceAccount from "../config/the-animals-are-starving-firebase-adminsdk-fbsvc-e38aec3b3e.json";

const serviceAccountKey: ServiceAccount = {
  projectId: serviceAccount.project_id,
  privateKey: serviceAccount.private_key,
  clientEmail: serviceAccount.client_email,
};

admin.initializeApp({
  credential: cert(serviceAccountKey),
});

export default admin;
