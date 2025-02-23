import express from "express";
import { getUserRankings, predictFeedingCost, detectAnomalies } from "../controllers/analyticsController";

const router = express.Router();

router.get("/rankings/:householdId", getUserRankings);
router.post("/feeding-cost/:householdId", predictFeedingCost); // Requires `pricePerKg` in body
router.get("/anomalies/:householdId", detectAnomalies);

export default router;
