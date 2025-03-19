import express from "express";
// import { getUserRankings, predictFeedingCost, detectAnomalies } from "../controllers/analyticsController";
import { getUserRankings } from "../controllers/analyticsController";

const router = express.Router();

router.get("/rankings/:householdId", getUserRankings);
// Not implemented for this milestone!
// router.post("/feeding-cost/:householdId", predictFeedingCost); // Requires `pricePerKg` in body
// router.get("/anomalies/:householdId", detectAnomalies);

export default router;
