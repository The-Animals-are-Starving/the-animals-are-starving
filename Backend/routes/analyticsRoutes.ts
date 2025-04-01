import express from "express";
import { getFeedingAnomalies, getUserRankings } from "../controllers/analyticsController";

const router = express.Router();

router.get("/rankings/:householdId", getUserRankings);
router.post("/anomalies/:householdId", getFeedingAnomalies);

export default router;
