import express from "express";
import { createHousehold} from "../controllers/userHouseholdController";

const router = express.Router();

router.post("/create", createHousehold);



export default router;