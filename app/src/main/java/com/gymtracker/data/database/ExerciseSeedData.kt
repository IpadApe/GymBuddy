package com.gymtracker.data.database

import com.gymtracker.data.database.entities.ExerciseEntity

object ExerciseSeedData {
    fun getExercises(): List<ExerciseEntity> = buildList {
        // ═══════════════════════ CHEST ═══════════════════════
        add(exercise("Barbell Bench Press", "Chest", "Shoulders,Triceps", "Barbell", "Push", "Beginner",
            "1. Lie on a flat bench, grip barbell slightly wider than shoulder-width.\n2. Unrack and lower bar to mid-chest.\n3. Press bar up to full arm extension.\n4. Repeat for desired reps."))
        add(exercise("Incline Barbell Bench Press", "Chest", "Shoulders,Triceps", "Barbell", "Push", "Intermediate",
            "1. Set bench to 30-45 degree incline.\n2. Grip barbell slightly wider than shoulder-width.\n3. Lower bar to upper chest.\n4. Press up to lockout."))
        add(exercise("Decline Barbell Bench Press", "Chest", "Triceps", "Barbell", "Push", "Intermediate",
            "1. Set bench to slight decline.\n2. Grip barbell shoulder-width apart.\n3. Lower bar to lower chest.\n4. Press up to lockout."))
        add(exercise("Dumbbell Bench Press", "Chest", "Shoulders,Triceps", "Dumbbell", "Push", "Beginner",
            "1. Lie on flat bench with dumbbells at chest level.\n2. Press dumbbells up until arms are extended.\n3. Lower slowly back to start.\n4. Keep feet flat on floor."))
        add(exercise("Incline Dumbbell Press", "Chest", "Shoulders,Triceps", "Dumbbell", "Push", "Beginner",
            "1. Set bench to 30-45 degrees.\n2. Press dumbbells from shoulder level.\n3. Extend arms fully at top.\n4. Lower with control."))
        add(exercise("Decline Dumbbell Press", "Chest", "Triceps", "Dumbbell", "Push", "Intermediate",
            "1. Lie on decline bench with dumbbells.\n2. Press dumbbells up from lower chest.\n3. Extend fully.\n4. Lower slowly."))
        add(exercise("Dumbbell Flyes", "Chest", "Shoulders", "Dumbbell", "Isolation", "Beginner",
            "1. Lie on flat bench, arms extended above chest.\n2. Lower dumbbells out to sides with slight elbow bend.\n3. Squeeze chest to bring dumbbells back.\n4. Maintain slight bend in elbows throughout."))
        add(exercise("Incline Dumbbell Flyes", "Chest", "Shoulders", "Dumbbell", "Isolation", "Intermediate",
            "1. Set bench to 30-45 degrees.\n2. Start with arms extended.\n3. Open arms wide with slight bend.\n4. Squeeze chest to close."))
        add(exercise("Cable Crossover", "Chest", "Shoulders", "Cable", "Isolation", "Intermediate",
            "1. Set cables at high position.\n2. Step forward with slight lean.\n3. Bring hands together in front of chest.\n4. Slowly return to start."))
        add(exercise("Low Cable Crossover", "Chest", "Shoulders", "Cable", "Isolation", "Intermediate",
            "1. Set cables at low position.\n2. Grip handles, step forward.\n3. Bring hands up and together.\n4. Control the negative."))
        add(exercise("Machine Chest Press", "Chest", "Shoulders,Triceps", "Machine", "Push", "Beginner",
            "1. Adjust seat height so handles are at chest level.\n2. Press handles forward.\n3. Extend arms fully.\n4. Return slowly."))
        add(exercise("Pec Deck Machine", "Chest", "Shoulders", "Machine", "Isolation", "Beginner",
            "1. Sit with back against pad.\n2. Place forearms on pads.\n3. Squeeze pads together.\n4. Return slowly."))
        add(exercise("Push-Ups", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Beginner",
            "1. Start in plank position, hands shoulder-width.\n2. Lower chest to floor.\n3. Push back up.\n4. Keep body in straight line."))
        add(exercise("Diamond Push-Ups", "Chest", "Triceps", "Bodyweight", "Push", "Intermediate",
            "1. Form diamond shape with hands.\n2. Lower chest to hands.\n3. Push back up.\n4. Keep elbows close to body."))
        add(exercise("Dips (Chest)", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Intermediate",
            "1. Grip parallel bars, lean forward slightly.\n2. Lower body until upper arms parallel to floor.\n3. Push back up.\n4. Keep forward lean for chest focus."))
        add(exercise("Landmine Press", "Chest", "Shoulders,Triceps", "Barbell", "Push", "Intermediate",
            "1. Place end of barbell in landmine.\n2. Hold other end at shoulder.\n3. Press up and forward.\n4. Lower with control."))
        add(exercise("Svend Press", "Chest", "", "Other", "Isolation", "Beginner",
            "1. Hold plates between palms at chest.\n2. Press forward while squeezing.\n3. Return to chest.\n4. Maintain squeeze throughout."))
        add(exercise("Smith Machine Bench Press", "Chest", "Shoulders,Triceps", "Smith Machine", "Push", "Beginner",
            "1. Lie on bench under Smith machine bar.\n2. Unrack and lower to chest.\n3. Press up to lockout.\n4. Re-rack."))

        // ── Chest Press Variations (from CSV) ────────────────
        add(exercise("Wide Grip Bench Press", "Chest", "Shoulders,Triceps", "Barbell", "Push", "Intermediate",
            "1. Lie on flat bench, grip bar wider than shoulder-width (pinky on rings).\n2. Lower bar to mid-chest with elbows flared.\n3. Press up to lockout.\n4. Wider grip shifts emphasis to outer chest."))
        add(exercise("Neutral Grip Dumbbell Press", "Chest", "Shoulders,Triceps", "Dumbbell", "Push", "Beginner",
            "1. Lie on flat bench, dumbbells held with palms facing each other.\n2. Press dumbbells up, keeping palms facing in.\n3. Lower slowly to chest level.\n4. Easier on shoulders than pronated grip."))
        add(exercise("Single Arm Dumbbell Press", "Chest", "Shoulders,Triceps", "Dumbbell", "Push", "Intermediate",
            "1. Lie on flat bench, one dumbbell held at chest.\n2. Brace core to prevent rotation.\n3. Press dumbbell up to lockout.\n4. Lower slowly, complete all reps before switching arms."))
        add(exercise("Alternating Dumbbell Press", "Chest", "Shoulders,Triceps", "Dumbbell", "Push", "Beginner",
            "1. Lie on flat bench, both dumbbells pressed up.\n2. Lower one dumbbell to chest while keeping other extended.\n3. Press back up, then lower the other side.\n4. Alternate in a controlled rhythm."))
        add(exercise("Paused Bench Press", "Chest", "Shoulders,Triceps", "Barbell", "Push", "Advanced",
            "1. Lie on bench, unrack bar.\n2. Lower bar to chest and pause for 1-2 seconds (no bouncing).\n3. Press up explosively from dead stop.\n4. Eliminates stretch reflex — builds true strength off the chest."))
        add(exercise("Incline Machine Press", "Chest", "Shoulders,Triceps", "Machine", "Push", "Beginner",
            "1. Adjust seat so handles are at upper-chest height on incline machine.\n2. Press handles forward and up.\n3. Extend arms fully without locking.\n4. Return slowly, feeling the stretch."))
        add(exercise("Resistance Band Chest Press", "Chest", "Shoulders,Triceps", "Band", "Push", "Beginner",
            "1. Anchor band behind you at chest height.\n2. Hold handles at chest, step forward for tension.\n3. Press hands forward until arms extended.\n4. Return slowly — band increases resistance at lockout."))
        add(exercise("Kettlebell Floor Press", "Chest", "Shoulders,Triceps", "Kettlebell", "Push", "Beginner",
            "1. Lie on floor, kettlebells in each hand at chest level.\n2. Press kettlebells up to full extension.\n3. Lower until triceps touch the floor — this limits ROM naturally.\n4. Great for shoulder-safe pressing."))

        // ── Chest Fly Variations (from CSV) ──────────────────
        add(exercise("Decline Dumbbell Fly", "Chest", "Shoulders", "Dumbbell", "Isolation", "Intermediate",
            "1. Lie on decline bench with dumbbells extended above lower chest.\n2. Lower dumbbells out to sides with slight elbow bend.\n3. Feel the stretch at the bottom.\n4. Squeeze chest to bring dumbbells back together."))
        add(exercise("Incline Cable Fly", "Chest", "Shoulders", "Cable", "Isolation", "Intermediate",
            "1. Set cables at low position, lie on incline bench between them.\n2. Hold handles above chest with slight elbow bend.\n3. Open arms wide and down until chest stretched.\n4. Squeeze to bring handles back up and together."))
        add(exercise("Resistance Band Chest Fly", "Chest", "Shoulders", "Band", "Isolation", "Beginner",
            "1. Anchor band at chest height behind you.\n2. Hold handles, arms out to sides with slight elbow bend.\n3. Bring hands together in front of chest, squeezing pecs.\n4. Return slowly with control."))
        add(exercise("Standing Cable Fly", "Chest", "Shoulders", "Cable", "Isolation", "Intermediate",
            "1. Set cables at mid-chest height, stand between them.\n2. Hold handles with slight elbow bend.\n3. Bring hands together in front of chest in arc motion.\n4. Return slowly — works chest through full range."))

        // ── Push-Up Variations (from CSV) ────────────────────
        add(exercise("Incline Push-Ups", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Beginner",
            "1. Place hands on elevated surface (bench or step) at shoulder-width.\n2. Lower chest to the surface.\n3. Push back up to start.\n4. Easier than floor push-ups — great for beginners."))
        add(exercise("Decline Push-Ups", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Intermediate",
            "1. Place feet on elevated surface (bench), hands on floor.\n2. Lower chest to floor, keeping body straight.\n3. Push back up.\n4. Shifts emphasis to upper chest — harder than standard."))
        add(exercise("Wide Push-Ups", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Beginner",
            "1. Place hands wider than shoulder-width on floor.\n2. Lower chest to floor with elbows flaring outward.\n3. Push back up.\n4. Wider hand placement emphasises outer chest."))
        add(exercise("Explosive Push-Ups", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Intermediate",
            "1. Start in standard push-up position.\n2. Lower to floor with control.\n3. Push up explosively so hands leave the floor.\n4. Land softly and immediately go into next rep. Builds chest power."))
        add(exercise("Clap Push-Ups", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Advanced",
            "1. Start in push-up position.\n2. Lower to floor.\n3. Push up explosively, clap hands in the air.\n4. Land softly with bent elbows and repeat. Maximum power output."))
        add(exercise("Deficit Push-Ups", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Intermediate",
            "1. Place hands on two raised surfaces (plates or push-up handles).\n2. Lower chest below hand level for extra range of motion.\n3. Push back up to start.\n4. Greater pec stretch than standard push-ups."))
        add(exercise("Weighted Push-Ups", "Chest", "Shoulders,Triceps", "Other", "Push", "Intermediate",
            "1. Place a weight plate on your upper back (have a partner assist).\n2. Perform standard push-ups with added load.\n3. Keep body rigid throughout.\n4. Progress push-up training beyond bodyweight."))
        add(exercise("Archer Push-Ups", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Advanced",
            "1. Hands placed wider than diamond, one arm straight to side.\n2. Lower chest toward the bent-arm side.\n3. Push back up.\n4. Alternate sides. Single-arm push-up progression."))

        // ── Chest Dip Variations (from CSV) ──────────────────
        add(exercise("Weighted Chest Dips", "Chest", "Shoulders,Triceps", "Other", "Push", "Advanced",
            "1. Attach weight belt with plates, or hold dumbbell between feet.\n2. Grip parallel bars, lean forward for chest emphasis.\n3. Lower until upper arms are parallel to floor.\n4. Press back up. Adds load beyond bodyweight for progression."))
        add(exercise("Assisted Chest Dips", "Chest", "Shoulders,Triceps", "Machine", "Push", "Beginner",
            "1. Use assisted dip machine — select counterweight to reduce effective bodyweight.\n2. Grip handles and lean slightly forward.\n3. Lower until elbows at 90 degrees.\n4. Press back up. Ideal for building strength toward unassisted dips."))

        // ═══════════════════════ BACK ═══════════════════════
        add(exercise("Barbell Deadlift", "Back", "Hamstrings,Glutes", "Barbell", "Hinge", "Intermediate",
            "1. Stand with feet hip-width, bar over mid-foot.\n2. Hinge at hips, grip bar.\n3. Drive through legs and extend hips.\n4. Stand tall, then lower with control."))
        add(exercise("Conventional Deadlift", "Back", "Hamstrings,Glutes", "Barbell", "Hinge", "Intermediate",
            "1. Feet hip-width apart under bar.\n2. Grip outside knees.\n3. Lift by extending knees and hips.\n4. Lock out at top."))
        add(exercise("Sumo Deadlift", "Back", "Glutes,Quads", "Barbell", "Hinge", "Advanced",
            "1. Wide stance, toes pointed out.\n2. Grip bar between legs.\n3. Drive hips forward.\n4. Stand tall at lockout."))
        add(exercise("Pull-Ups", "Back", "Biceps", "Bodyweight", "Pull", "Intermediate",
            "1. Grip bar with overhand grip, shoulder-width.\n2. Pull body up until chin over bar.\n3. Lower with control.\n4. Full dead hang at bottom."))
        add(exercise("Chin-Ups", "Back", "Biceps", "Bodyweight", "Pull", "Intermediate",
            "1. Grip bar with underhand grip.\n2. Pull up until chin over bar.\n3. Lower slowly.\n4. Engage back muscles."))
        add(exercise("Lat Pulldown", "Back", "Biceps", "Cable", "Pull", "Beginner",
            "1. Sit at lat pulldown, grip bar wide.\n2. Pull bar down to upper chest.\n3. Squeeze shoulder blades.\n4. Return slowly."))
        add(exercise("Close-Grip Lat Pulldown", "Back", "Biceps", "Cable", "Pull", "Beginner",
            "1. Use V-bar or close grip handle.\n2. Pull to upper chest.\n3. Squeeze at bottom.\n4. Control the return."))
        add(exercise("Barbell Row", "Back", "Biceps", "Barbell", "Pull", "Intermediate",
            "1. Bend at hips, slight knee bend.\n2. Grip barbell overhand.\n3. Row to lower chest.\n4. Lower with control."))
        add(exercise("Pendlay Row", "Back", "Biceps", "Barbell", "Pull", "Advanced",
            "1. Start with bar on floor each rep.\n2. Explosive row to chest.\n3. Lower back to floor.\n4. Reset between reps."))
        add(exercise("Dumbbell Row", "Back", "Biceps", "Dumbbell", "Pull", "Beginner",
            "1. Place one hand and knee on bench.\n2. Row dumbbell to hip.\n3. Squeeze at top.\n4. Lower slowly."))
        add(exercise("T-Bar Row", "Back", "Biceps", "Barbell", "Pull", "Intermediate",
            "1. Straddle T-bar, grip handle.\n2. Row weight to chest.\n3. Squeeze back at top.\n4. Lower with control."))
        add(exercise("Seated Cable Row", "Back", "Biceps", "Cable", "Pull", "Beginner",
            "1. Sit at cable row machine.\n2. Pull handle to torso.\n3. Squeeze shoulder blades.\n4. Return slowly."))
        add(exercise("Face Pull", "Back", "Shoulders", "Cable", "Pull", "Beginner",
            "1. Set cable at face height.\n2. Pull rope to face.\n3. Spread rope apart at end.\n4. Control return."))
        add(exercise("Straight Arm Pulldown", "Back", "", "Cable", "Pull", "Intermediate",
            "1. Stand facing cable with bar at top.\n2. Keep arms straight, pull down.\n3. Squeeze lats at bottom.\n4. Return slowly."))
        add(exercise("Machine Row", "Back", "Biceps", "Machine", "Pull", "Beginner",
            "1. Sit at machine, chest against pad.\n2. Pull handles back.\n3. Squeeze shoulder blades.\n4. Return slowly."))
        add(exercise("Inverted Row", "Back", "Biceps", "Bodyweight", "Pull", "Beginner",
            "1. Lie under bar at waist height.\n2. Grip bar, body straight.\n3. Pull chest to bar.\n4. Lower with control."))
        add(exercise("Rack Pull", "Back", "Hamstrings", "Barbell", "Hinge", "Intermediate",
            "1. Set bar at knee height in rack.\n2. Grip and deadlift from pins.\n3. Lockout at top.\n4. Lower to pins."))
        add(exercise("Meadows Row", "Back", "Biceps", "Barbell", "Pull", "Advanced",
            "1. Stand perpendicular to landmine.\n2. Row with overhand grip.\n3. Pull to hip.\n4. Control the negative."))
        add(exercise("Chest Supported Row", "Back", "Biceps", "Dumbbell", "Pull", "Beginner",
            "1. Lie face down on incline bench.\n2. Row dumbbells up.\n3. Squeeze at top.\n4. Lower slowly."))

        // ═══════════════════════ SHOULDERS ═══════════════════════
        add(exercise("Overhead Press", "Shoulders", "Triceps", "Barbell", "Push", "Intermediate",
            "1. Grip barbell at shoulder width.\n2. Press overhead to lockout.\n3. Lower to shoulders.\n4. Keep core braced."))
        add(exercise("Dumbbell Shoulder Press", "Shoulders", "Triceps", "Dumbbell", "Push", "Beginner",
            "1. Sit with dumbbells at shoulder height.\n2. Press overhead.\n3. Lower to shoulders.\n4. Keep back against bench."))
        add(exercise("Arnold Press", "Shoulders", "Triceps", "Dumbbell", "Push", "Intermediate",
            "1. Start with palms facing you at shoulder.\n2. Rotate and press up.\n3. Reverse the rotation down.\n4. Control the movement."))
        add(exercise("Lateral Raise", "Shoulders", "", "Dumbbell", "Isolation", "Beginner",
            "1. Stand with dumbbells at sides.\n2. Raise arms to sides, shoulder height.\n3. Slight bend in elbows.\n4. Lower slowly."))
        add(exercise("Cable Lateral Raise", "Shoulders", "", "Cable", "Isolation", "Beginner",
            "1. Stand beside cable machine.\n2. Raise arm to side.\n3. Shoulder height.\n4. Control the return."))
        add(exercise("Front Raise", "Shoulders", "", "Dumbbell", "Isolation", "Beginner",
            "1. Stand with dumbbells in front.\n2. Raise one arm to shoulder height.\n3. Lower slowly.\n4. Alternate arms."))
        add(exercise("Reverse Flyes", "Shoulders", "Back", "Dumbbell", "Isolation", "Beginner",
            "1. Bend forward at hips.\n2. Raise dumbbells to sides.\n3. Squeeze rear delts.\n4. Lower slowly."))
        add(exercise("Machine Shoulder Press", "Shoulders", "Triceps", "Machine", "Push", "Beginner",
            "1. Sit at machine, grip handles.\n2. Press overhead.\n3. Extend fully.\n4. Lower with control."))
        add(exercise("Upright Row", "Shoulders", "Biceps", "Barbell", "Pull", "Intermediate",
            "1. Grip barbell narrow.\n2. Pull up along body.\n3. Elbows go above shoulders.\n4. Lower slowly."))
        add(exercise("Dumbbell Shrugs", "Shoulders", "", "Dumbbell", "Isolation", "Beginner",
            "1. Hold dumbbells at sides.\n2. Shrug shoulders up.\n3. Hold at top.\n4. Lower slowly."))
        add(exercise("Barbell Shrugs", "Shoulders", "", "Barbell", "Isolation", "Beginner",
            "1. Hold barbell in front of thighs.\n2. Shrug up.\n3. Hold briefly.\n4. Lower."))
        add(exercise("Behind Neck Press", "Shoulders", "Triceps", "Barbell", "Push", "Advanced",
            "1. Grip barbell wide behind head.\n2. Press up to lockout.\n3. Lower behind neck.\n4. Requires good mobility."))
        add(exercise("Pike Push-Ups", "Shoulders", "Triceps", "Bodyweight", "Push", "Intermediate",
            "1. Start in downward dog position.\n2. Bend elbows, lower head toward floor.\n3. Push back up.\n4. Keep hips high."))
        add(exercise("Lu Raises", "Shoulders", "", "Dumbbell", "Isolation", "Advanced",
            "1. Perform lateral raise.\n2. At top, press dumbbells overhead.\n3. Lower to lateral position.\n4. Return to start."))
        add(exercise("Z Press", "Shoulders", "Triceps", "Barbell", "Push", "Advanced",
            "1. Sit on floor, legs extended.\n2. Press barbell overhead.\n3. No back support.\n4. Requires core stability."))

        // ═══════════════════════ BICEPS ═══════════════════════
        add(exercise("Barbell Curl", "Biceps", "Forearms", "Barbell", "Isolation", "Beginner",
            "1. Stand with barbell, underhand grip.\n2. Curl bar to shoulders.\n3. Keep elbows stationary.\n4. Lower slowly."))
        add(exercise("EZ Bar Curl", "Biceps", "Forearms", "EZ Bar", "Isolation", "Beginner",
            "1. Grip EZ bar at angled portions.\n2. Curl to shoulders.\n3. Keep elbows pinned.\n4. Lower with control."))
        add(exercise("Dumbbell Curl", "Biceps", "Forearms", "Dumbbell", "Isolation", "Beginner",
            "1. Stand with dumbbells at sides.\n2. Curl with supinated grip.\n3. Squeeze at top.\n4. Lower slowly."))
        add(exercise("Hammer Curl", "Biceps", "Forearms", "Dumbbell", "Isolation", "Beginner",
            "1. Hold dumbbells with neutral grip.\n2. Curl while keeping palms facing each other.\n3. Squeeze at top.\n4. Lower slowly."))
        add(exercise("Incline Dumbbell Curl", "Biceps", "", "Dumbbell", "Isolation", "Intermediate",
            "1. Sit on incline bench (45 degrees).\n2. Let arms hang.\n3. Curl dumbbells up.\n4. Stretch at bottom."))
        add(exercise("Preacher Curl", "Biceps", "", "EZ Bar", "Isolation", "Beginner",
            "1. Rest arms on preacher bench.\n2. Curl bar up.\n3. Squeeze at top.\n4. Lower slowly."))
        add(exercise("Concentration Curl", "Biceps", "", "Dumbbell", "Isolation", "Beginner",
            "1. Sit with elbow on inner thigh.\n2. Curl dumbbell to shoulder.\n3. Squeeze at top.\n4. Lower slowly."))
        add(exercise("Cable Curl", "Biceps", "", "Cable", "Isolation", "Beginner",
            "1. Stand at cable machine.\n2. Curl bar up.\n3. Keep elbows stationary.\n4. Control the return."))
        add(exercise("Spider Curl", "Biceps", "", "Dumbbell", "Isolation", "Intermediate",
            "1. Lie face down on incline bench.\n2. Arms hanging down.\n3. Curl dumbbells up.\n4. Maximum contraction."))
        add(exercise("Bayesian Curl", "Biceps", "", "Cable", "Isolation", "Intermediate",
            "1. Stand facing away from cable.\n2. Arm extended behind.\n3. Curl forward.\n4. Full stretch and squeeze."))
        add(exercise("21s", "Biceps", "", "Barbell", "Isolation", "Advanced",
            "1. 7 reps bottom half.\n2. 7 reps top half.\n3. 7 full range reps.\n4. Intense pump."))

        // ═══════════════════════ TRICEPS ═══════════════════════
        add(exercise("Tricep Pushdown", "Triceps", "", "Cable", "Isolation", "Beginner",
            "1. Stand at cable with rope/bar.\n2. Push down until arms straight.\n3. Keep elbows at sides.\n4. Return slowly."))
        add(exercise("Overhead Tricep Extension", "Triceps", "", "Dumbbell", "Isolation", "Beginner",
            "1. Hold dumbbell overhead with both hands.\n2. Lower behind head.\n3. Extend back up.\n4. Keep elbows close."))
        add(exercise("Skull Crushers", "Triceps", "", "EZ Bar", "Isolation", "Intermediate",
            "1. Lie on bench, hold EZ bar overhead.\n2. Lower bar to forehead.\n3. Extend back up.\n4. Keep upper arms vertical."))
        add(exercise("Close Grip Bench Press", "Triceps", "Chest", "Barbell", "Push", "Intermediate",
            "1. Lie on bench, narrow grip.\n2. Lower bar to lower chest.\n3. Press up focusing on triceps.\n4. Lock out at top."))
        add(exercise("Tricep Dips", "Triceps", "Chest", "Bodyweight", "Push", "Intermediate",
            "1. Grip parallel bars, body upright.\n2. Lower until elbows at 90 degrees.\n3. Push back up.\n4. Keep torso vertical."))
        add(exercise("Tricep Kickback", "Triceps", "", "Dumbbell", "Isolation", "Beginner",
            "1. Bend forward, upper arm parallel to floor.\n2. Extend forearm back.\n3. Squeeze at lockout.\n4. Lower slowly."))
        add(exercise("Cable Overhead Extension", "Triceps", "", "Cable", "Isolation", "Beginner",
            "1. Face away from cable, rope behind head.\n2. Extend forward and up.\n3. Squeeze at extension.\n4. Return slowly."))
        add(exercise("Bench Dips", "Triceps", "Chest", "Bodyweight", "Push", "Beginner",
            "1. Hands on bench behind you.\n2. Lower body by bending elbows.\n3. Push back up.\n4. Feet on floor or elevated."))
        add(exercise("JM Press", "Triceps", "Chest", "Barbell", "Push", "Advanced",
            "1. Hybrid of close grip bench and skull crusher.\n2. Lower to chin level.\n3. Press up and back.\n4. Advanced movement."))
        add(exercise("French Press", "Triceps", "", "EZ Bar", "Isolation", "Intermediate",
            "1. Seated or standing, bar overhead.\n2. Lower behind head.\n3. Extend up.\n4. Full stretch at bottom."))
        add(exercise("Straight Bar Tricep Pushdown", "Triceps", "", "Cable", "Isolation", "Beginner",
            "1. Stand at cable with straight bar attachment.\n2. Grip bar overhand, elbows at sides.\n3. Push bar down until arms fully extended.\n4. Control the return slowly."))
        add(exercise("V-Bar Tricep Pushdown", "Triceps", "", "Cable", "Isolation", "Beginner",
            "1. Attach V-bar to cable.\n2. Grip V-bar, elbows tucked to sides.\n3. Push down until arms locked out.\n4. Squeeze triceps at bottom."))
        add(exercise("Triangle Bar Tricep Pushdown", "Triceps", "", "Cable", "Isolation", "Beginner",
            "1. Attach triangle/close-grip bar to cable.\n2. Grip bar with palms facing each other.\n3. Push down until arms extended.\n4. Slow return to maintain tension."))
        add(exercise("Reverse Grip Bench Press", "Chest", "Triceps", "Barbell", "Push", "Intermediate",
            "1. Lie on flat bench, grip bar with palms facing you (supinated).\n2. Unrack and lower bar to lower chest.\n3. Press up explosively.\n4. Hits upper chest and triceps."))

        // ═══════════════════════ BACK (additional) ═══════════════
        add(exercise("Back Extension", "Back", "Glutes,Hamstrings", "Machine", "Hinge", "Beginner",
            "1. Lie face down on hyperextension bench, hips at edge.\n2. Lower torso toward floor.\n3. Raise back up until body is straight (don't hyperextend).\n4. Control the movement throughout."))
        add(exercise("Romanian Deadlift", "Back", "Hamstrings,Glutes", "Barbell", "Hinge", "Intermediate",
            "1. Stand holding bar at hip level.\n2. Hinge at hips, pushing them back while keeping back flat.\n3. Lower bar along legs until you feel hamstring stretch.\n4. Drive hips forward to stand back up."))
        add(exercise("Good Morning", "Back", "Hamstrings,Glutes", "Barbell", "Hinge", "Intermediate",
            "1. Bar on upper back, feet shoulder-width.\n2. Hinge at hips, keeping back flat and knees slightly bent.\n3. Lower until torso is near parallel to floor.\n4. Drive hips forward to return."))

        // ═══════════════════════ CORE (additional) ════════════════
        add(exercise("Side Bend", "Abs/Core", "Obliques", "Dumbbell", "Isolation", "Beginner",
            "1. Stand with dumbbell in one hand at side.\n2. Bend laterally toward the dumbbell side.\n3. Return to upright, then bend away to stretch obliques.\n4. Complete reps on one side before switching."))
        add(exercise("Wood Chop", "Abs/Core", "Shoulders,Back", "Cable", "Isolation", "Beginner",
            "1. Set cable at high position, stand sideways to machine.\n2. Pull cable diagonally down and across body.\n3. Rotate through core, not just arms.\n4. Control the return."))
        add(exercise("Reverse Crunch", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Lie on back, hands under hips for support.\n2. Bring knees toward chest, lifting hips off floor.\n3. Lower legs back down slowly.\n4. Keep lower back pressed to floor throughout."))

        // ═══════════════════════ FOREARMS ═══════════════════════
        add(exercise("Wrist Curl", "Forearms", "", "Barbell", "Isolation", "Beginner",
            "1. Sit with forearms on thighs.\n2. Curl wrists up.\n3. Lower slowly.\n4. Full range of motion."))
        add(exercise("Reverse Wrist Curl", "Forearms", "", "Barbell", "Isolation", "Beginner",
            "1. Sit with forearms on thighs, palms down.\n2. Extend wrists up.\n3. Lower slowly.\n4. Lighter weight needed."))
        add(exercise("Farmer's Walk", "Forearms", "Shoulders", "Dumbbell", "Carry", "Beginner",
            "1. Hold heavy dumbbells at sides.\n2. Walk with upright posture.\n3. Maintain grip.\n4. Walk set distance."))
        add(exercise("Plate Pinch Hold", "Forearms", "", "Other", "Isolation", "Intermediate",
            "1. Pinch two plates together.\n2. Hold with fingertips.\n3. Timed hold.\n4. Builds grip strength."))
        add(exercise("Dead Hang", "Forearms", "Back", "Bodyweight", "Isolation", "Beginner",
            "1. Hang from pull-up bar.\n2. Arms fully extended.\n3. Hold for time.\n4. Builds grip endurance."))

        // ═══════════════════════ ABS/CORE ═══════════════════════
        add(exercise("Plank", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Forearms on floor, body straight.\n2. Engage core.\n3. Hold position.\n4. Don't let hips sag."))
        add(exercise("Crunches", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Lie on back, knees bent.\n2. Curl shoulders up.\n3. Squeeze abs at top.\n4. Lower slowly."))
        add(exercise("Hanging Leg Raise", "Abs/Core", "", "Bodyweight", "Isolation", "Intermediate",
            "1. Hang from pull-up bar.\n2. Raise legs to parallel.\n3. Control the lower.\n4. Avoid swinging."))
        add(exercise("Ab Wheel Rollout", "Abs/Core", "", "Other", "Isolation", "Advanced",
            "1. Kneel with ab wheel.\n2. Roll forward extending body.\n3. Pull back with abs.\n4. Maintain flat back."))
        add(exercise("Cable Crunch", "Abs/Core", "", "Cable", "Isolation", "Beginner",
            "1. Kneel at cable machine.\n2. Hold rope behind head.\n3. Crunch down.\n4. Squeeze abs at bottom."))
        add(exercise("Russian Twist", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Sit with knees bent, lean back.\n2. Twist torso side to side.\n3. Hold weight for added resistance.\n4. Control the rotation."))
        add(exercise("Mountain Climbers", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Start in push-up position.\n2. Drive knees to chest alternately.\n3. Keep core engaged.\n4. Maintain pace."))
        add(exercise("Bicycle Crunch", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Lie on back, hands behind head.\n2. Alternate elbow to opposite knee.\n3. Extend other leg.\n4. Controlled pace."))
        add(exercise("Pallof Press", "Abs/Core", "", "Cable", "Isolation", "Intermediate",
            "1. Stand perpendicular to cable.\n2. Hold handle at chest.\n3. Press forward.\n4. Resist rotation."))
        add(exercise("Dragon Flag", "Abs/Core", "", "Bodyweight", "Isolation", "Advanced",
            "1. Lie on bench, grip behind head.\n2. Raise body as one unit.\n3. Lower slowly.\n4. Extreme core strength."))
        add(exercise("Decline Sit-Up", "Abs/Core", "", "Bodyweight", "Isolation", "Intermediate",
            "1. Secure feet on decline bench.\n2. Lower torso back.\n3. Sit up fully.\n4. Add weight for difficulty."))
        add(exercise("V-Ups", "Abs/Core", "", "Bodyweight", "Isolation", "Intermediate",
            "1. Lie flat on back.\n2. Simultaneously raise legs and torso.\n3. Touch toes at top.\n4. Lower with control."))
        add(exercise("Side Plank", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Lie on side, forearm on floor.\n2. Raise hips off floor.\n3. Body in straight line.\n4. Hold for time."))

        // ═══════════════════════ QUADS ═══════════════════════
        add(exercise("Barbell Back Squat", "Quads", "Glutes,Hamstrings", "Barbell", "Squat", "Intermediate",
            "1. Bar on upper back, feet shoulder-width.\n2. Squat down to parallel or below.\n3. Drive up through heels.\n4. Keep chest up."))
        add(exercise("Front Squat", "Quads", "Glutes", "Barbell", "Squat", "Advanced",
            "1. Bar on front delts, elbows high.\n2. Squat deep.\n3. Drive up.\n4. Keeps torso more upright."))
        add(exercise("Goblet Squat", "Quads", "Glutes", "Dumbbell", "Squat", "Beginner",
            "1. Hold dumbbell at chest.\n2. Squat between knees.\n3. Drive up.\n4. Great for learning squat form."))
        add(exercise("Leg Press", "Quads", "Glutes,Hamstrings", "Machine", "Squat", "Beginner",
            "1. Sit in leg press, feet shoulder-width.\n2. Lower platform.\n3. Press up.\n4. Don't lock knees fully."))
        add(exercise("Hack Squat", "Quads", "Glutes", "Machine", "Squat", "Intermediate",
            "1. Stand in hack squat machine.\n2. Lower by bending knees.\n3. Press up.\n4. Feet lower on platform targets quads."))
        add(exercise("Bulgarian Split Squat", "Quads", "Glutes", "Dumbbell", "Squat", "Intermediate",
            "1. Rear foot elevated on bench.\n2. Squat down on front leg.\n3. Drive up through front foot.\n4. Keep torso upright."))
        add(exercise("Leg Extension", "Quads", "", "Machine", "Isolation", "Beginner",
            "1. Sit at leg extension machine.\n2. Extend legs fully.\n3. Squeeze quads at top.\n4. Lower slowly."))
        add(exercise("Walking Lunges", "Quads", "Glutes,Hamstrings", "Dumbbell", "Squat", "Beginner",
            "1. Step forward into lunge.\n2. Lower back knee toward floor.\n3. Drive forward off front foot.\n4. Alternate legs."))
        add(exercise("Step-Ups", "Quads", "Glutes", "Dumbbell", "Squat", "Beginner",
            "1. Stand facing bench/step.\n2. Step up with one foot.\n3. Drive to standing.\n4. Step down slowly."))
        add(exercise("Sissy Squat", "Quads", "", "Bodyweight", "Isolation", "Advanced",
            "1. Stand holding support.\n2. Lean back, bending knees.\n3. Lower until quads stretched.\n4. Push back up."))
        add(exercise("Wall Sit", "Quads", "", "Bodyweight", "Isolation", "Beginner",
            "1. Back against wall.\n2. Slide down to 90 degrees.\n3. Hold position.\n4. Thighs parallel to floor."))
        add(exercise("Smith Machine Squat", "Quads", "Glutes", "Smith Machine", "Squat", "Beginner",
            "1. Bar on upper back in Smith machine.\n2. Squat down.\n3. Press up.\n4. Guided bar path."))
        add(exercise("Pistol Squat", "Quads", "Glutes", "Bodyweight", "Squat", "Advanced",
            "1. Stand on one leg.\n2. Squat down fully.\n3. Other leg extended.\n4. Stand back up."))

        // ═══════════════════════ HAMSTRINGS ═══════════════════════
        add(exercise("Romanian Deadlift", "Hamstrings", "Glutes,Back", "Barbell", "Hinge", "Intermediate",
            "1. Hold barbell at hip height.\n2. Hinge at hips, push hips back.\n3. Lower bar along legs.\n4. Drive hips forward to stand."))
        add(exercise("Dumbbell Romanian Deadlift", "Hamstrings", "Glutes", "Dumbbell", "Hinge", "Beginner",
            "1. Hold dumbbells at thighs.\n2. Hinge at hips.\n3. Lower dumbbells.\n4. Squeeze glutes to stand."))
        add(exercise("Lying Leg Curl", "Hamstrings", "", "Machine", "Isolation", "Beginner",
            "1. Lie face down on machine.\n2. Curl pad toward glutes.\n3. Squeeze at top.\n4. Lower slowly."))
        add(exercise("Seated Leg Curl", "Hamstrings", "", "Machine", "Isolation", "Beginner",
            "1. Sit at machine.\n2. Curl pad down and back.\n3. Squeeze hamstrings.\n4. Return slowly."))
        add(exercise("Stiff Leg Deadlift", "Hamstrings", "Glutes,Back", "Barbell", "Hinge", "Intermediate",
            "1. Stand with barbell, slight knee bend.\n2. Hinge forward keeping legs mostly straight.\n3. Lower until stretch felt.\n4. Return to standing."))
        add(exercise("Good Mornings", "Hamstrings", "Back,Glutes", "Barbell", "Hinge", "Intermediate",
            "1. Bar on upper back.\n2. Hinge forward at hips.\n3. Keep back straight.\n4. Return to standing."))
        add(exercise("Nordic Hamstring Curl", "Hamstrings", "", "Bodyweight", "Isolation", "Advanced",
            "1. Kneel with ankles secured.\n2. Slowly lower torso forward.\n3. Resist with hamstrings.\n4. Push back up."))
        add(exercise("Glute Ham Raise", "Hamstrings", "Glutes", "Machine", "Isolation", "Advanced",
            "1. Secure feet in GHR machine.\n2. Lower torso forward.\n3. Curl back up using hamstrings.\n4. Full extension."))
        add(exercise("Single Leg Deadlift", "Hamstrings", "Glutes", "Dumbbell", "Hinge", "Intermediate",
            "1. Stand on one leg.\n2. Hinge forward.\n3. Extend opposite leg back.\n4. Return to standing."))
        add(exercise("Kettlebell Swing", "Hamstrings", "Glutes,Back", "Kettlebell", "Hinge", "Intermediate",
            "1. Hinge at hips, swing kettlebell back.\n2. Explosively drive hips forward.\n3. Swing to chest height.\n4. Control the swing back."))

        // ═══════════════════════ GLUTES ═══════════════════════
        add(exercise("Hip Thrust", "Glutes", "Hamstrings", "Barbell", "Hinge", "Intermediate",
            "1. Upper back on bench, barbell on hips.\n2. Drive hips up.\n3. Squeeze glutes at top.\n4. Lower with control."))
        add(exercise("Glute Bridge", "Glutes", "Hamstrings", "Bodyweight", "Hinge", "Beginner",
            "1. Lie on back, knees bent.\n2. Drive hips up.\n3. Squeeze glutes.\n4. Lower slowly."))
        add(exercise("Cable Pull-Through", "Glutes", "Hamstrings", "Cable", "Hinge", "Beginner",
            "1. Face away from cable.\n2. Hinge at hips.\n3. Pull through legs.\n4. Squeeze glutes at top."))
        add(exercise("Sumo Squat", "Glutes", "Quads", "Dumbbell", "Squat", "Beginner",
            "1. Wide stance, toes out.\n2. Hold dumbbell between legs.\n3. Squat down.\n4. Drive up squeezing glutes."))
        add(exercise("Donkey Kicks", "Glutes", "", "Bodyweight", "Isolation", "Beginner",
            "1. On all fours.\n2. Kick one leg back and up.\n3. Squeeze glute at top.\n4. Lower slowly."))
        add(exercise("Fire Hydrants", "Glutes", "", "Bodyweight", "Isolation", "Beginner",
            "1. On all fours.\n2. Raise knee to side.\n3. Keep 90-degree bend.\n4. Lower slowly."))
        add(exercise("Hip Abduction Machine", "Glutes", "", "Machine", "Isolation", "Beginner",
            "1. Sit at machine.\n2. Push legs apart.\n3. Squeeze outer glutes.\n4. Return slowly."))
        add(exercise("Reverse Lunge", "Glutes", "Quads", "Dumbbell", "Squat", "Beginner",
            "1. Step backward into lunge.\n2. Lower back knee.\n3. Drive through front heel.\n4. Return to standing."))

        // ═══════════════════════ CALVES ═══════════════════════
        add(exercise("Standing Calf Raise", "Calves", "", "Machine", "Isolation", "Beginner",
            "1. Shoulders under pads.\n2. Rise up on toes.\n3. Hold at top.\n4. Lower below platform level."))
        add(exercise("Seated Calf Raise", "Calves", "", "Machine", "Isolation", "Beginner",
            "1. Sit with knees under pad.\n2. Rise up on toes.\n3. Squeeze at top.\n4. Lower with full stretch."))
        add(exercise("Donkey Calf Raise", "Calves", "", "Machine", "Isolation", "Intermediate",
            "1. Lean forward, hips loaded.\n2. Rise on toes.\n3. Full stretch at bottom.\n4. Strong squeeze at top."))
        add(exercise("Single Leg Calf Raise", "Calves", "", "Bodyweight", "Isolation", "Beginner",
            "1. Stand on one foot on step.\n2. Rise up on toes.\n3. Hold at top.\n4. Lower below step level."))
        add(exercise("Smith Machine Calf Raise", "Calves", "", "Smith Machine", "Isolation", "Beginner",
            "1. Stand on block under Smith bar.\n2. Rise on toes.\n3. Controlled movement.\n4. Full range of motion."))
        add(exercise("Leg Press Calf Raise", "Calves", "", "Machine", "Isolation", "Beginner",
            "1. Feet at bottom of leg press platform.\n2. Press with toes.\n3. Full extension.\n4. Stretch at bottom."))
        add(exercise("Jump Rope", "Calves", "", "Other", "Isolation", "Beginner",
            "1. Hold rope handles.\n2. Jump continuously.\n3. Stay on balls of feet.\n4. Great for calf endurance."))

        // ═══════════════════════ COMPOUND / FULL BODY ═══════════════════════
        add(exercise("Clean and Press", "Shoulders", "Back,Quads,Glutes", "Barbell", "Push", "Advanced",
            "1. Start with bar on floor.\n2. Clean to shoulders.\n3. Press overhead.\n4. Lower and repeat."))
        add(exercise("Power Clean", "Back", "Quads,Shoulders,Glutes", "Barbell", "Pull", "Advanced",
            "1. Bar on floor, explosive pull.\n2. Catch at shoulders.\n3. Stand tall.\n4. Lower and repeat."))
        add(exercise("Thruster", "Quads", "Shoulders,Glutes", "Barbell", "Squat", "Intermediate",
            "1. Front squat position.\n2. Squat down.\n3. Drive up and press overhead.\n4. One fluid motion."))
        add(exercise("Man Maker", "Chest", "Back,Shoulders,Quads", "Dumbbell", "Push", "Advanced",
            "1. Push-up with dumbbells.\n2. Row each side.\n3. Jump feet to hands.\n4. Clean and press overhead."))
        add(exercise("Turkish Get-Up", "Abs/Core", "Shoulders,Glutes", "Kettlebell", "Carry", "Advanced",
            "1. Lie with kettlebell pressed overhead.\n2. Stand up in stages.\n3. Reverse the motion.\n4. Keep arm locked out."))
        add(exercise("Barbell Complex", "Back", "Shoulders,Quads,Glutes", "Barbell", "Pull", "Advanced",
            "1. Perform deadlift, row, clean, press.\n2. Without putting bar down.\n3. Set number of reps each.\n4. Intense full body."))
        add(exercise("Burpees", "Abs/Core", "Chest,Quads", "Bodyweight", "Push", "Beginner",
            "1. Drop to push-up.\n2. Perform push-up.\n3. Jump feet to hands.\n4. Jump up with arms overhead."))
        add(exercise("Battle Ropes", "Shoulders", "Abs/Core,Forearms", "Other", "Carry", "Intermediate",
            "1. Hold rope ends.\n2. Wave arms alternately.\n3. Or slam simultaneously.\n4. Full body conditioning."))
        add(exercise("Box Jumps", "Quads", "Glutes,Calves", "Other", "Squat", "Intermediate",
            "1. Stand facing box.\n2. Jump onto box.\n3. Land softly.\n4. Step down and repeat."))
        add(exercise("Sled Push", "Quads", "Glutes,Calves", "Other", "Carry", "Intermediate",
            "1. Lean into sled.\n2. Drive with legs.\n3. Push for distance.\n4. Full lower body."))

        // ═══════════════════════ CARDIO ═══════════════════════
        add(exercise("Running", "Cardio", "", "Other", "Cardio", "Beginner",
            "1. Warm up with 5 min brisk walk.\n2. Maintain steady pace, land mid-foot.\n3. Keep arms relaxed at 90°.\n4. Breathe rhythmically and cool down."))
        add(exercise("Walking", "Cardio", "", "Other", "Cardio", "Beginner",
            "1. Stand tall with core lightly engaged.\n2. Step heel-to-toe at a comfortable pace.\n3. Swing arms naturally.\n4. Aim for 30+ minutes for cardio benefit."))
        add(exercise("Jogging", "Cardio", "", "Other", "Cardio", "Beginner",
            "1. Start at a pace where you can hold a conversation.\n2. Land mid-foot beneath your hips.\n3. Keep shoulders relaxed.\n4. Build duration before increasing speed."))
        add(exercise("Sprinting", "Cardio", "Quads,Hamstrings,Calves,Glutes", "Other", "Cardio", "Advanced",
            "1. Drive off back foot explosively.\n2. Pump arms at 90° aggressively.\n3. Stay on balls of feet.\n4. Sprint 20–100 m, recover, repeat."))
        add(exercise("Cycling", "Cardio", "Quads,Hamstrings,Calves", "Machine", "Cardio", "Beginner",
            "1. Adjust seat so leg is almost fully extended at bottom.\n2. Pedal at a smooth cadence (70–90 rpm).\n3. Keep core engaged and back neutral.\n4. Use resistance to control intensity."))
        add(exercise("Stationary Bike", "Cardio", "Quads,Hamstrings,Calves", "Machine", "Cardio", "Beginner",
            "1. Set seat height to hip level.\n2. Pedal at 70–90 rpm.\n3. Vary resistance for interval training.\n4. Keep upper body relaxed."))
        add(exercise("Rowing Machine", "Cardio", "Back,Shoulders,Biceps,Quads", "Machine", "Cardio", "Beginner",
            "1. Sit with feet strapped, knees bent.\n2. Drive with legs first, then lean back, then pull arms.\n3. Return: arms out, lean forward, bend knees.\n4. Maintain smooth rhythm."))
        add(exercise("Treadmill Walk/Run", "Cardio", "", "Machine", "Cardio", "Beginner",
            "1. Set treadmill to warm-up speed.\n2. Maintain upright posture, slight forward lean.\n3. Adjust speed and incline as needed.\n4. Cool down last 5 minutes."))
        add(exercise("Elliptical Trainer", "Cardio", "Quads,Hamstrings,Glutes", "Machine", "Cardio", "Beginner",
            "1. Step onto pedals and grip handles lightly.\n2. Push and pull arms in sync with legs.\n3. Keep heels flat on pedals.\n4. Maintain steady cadence."))
        add(exercise("Stair Climber", "Cardio", "Quads,Glutes,Calves", "Machine", "Cardio", "Intermediate",
            "1. Step onto machine, keep slight forward lean.\n2. Drive through each step pushing down.\n3. Avoid leaning heavily on handrails.\n4. Adjust speed to maintain target heart rate."))
        add(exercise("Jump Rope", "Cardio", "Calves,Shoulders", "Other", "Cardio", "Beginner",
            "1. Hold handles at hip level, elbows slightly bent.\n2. Turn rope with wrists, not arms.\n3. Jump 1–2 inches off ground on balls of feet.\n4. Start with 30 sec on / 30 sec off."))
        add(exercise("Swimming", "Cardio", "Back,Shoulders,Chest", "Other", "Cardio", "Beginner",
            "1. Choose a stroke (freestyle, breaststroke, backstroke).\n2. Breathe rhythmically to one side.\n3. Keep body horizontal and core tight.\n4. Aim for consistent lap pace."))
        add(exercise("Hiking", "Cardio", "Quads,Hamstrings,Calves,Glutes", "Other", "Cardio", "Beginner",
            "1. Wear supportive footwear.\n2. Start on flat terrain, progress to inclines.\n3. Use trekking poles for balance on uneven ground.\n4. Maintain steady breathing pace."))
        add(exercise("HIIT Intervals", "Cardio", "", "Bodyweight", "Cardio", "Intermediate",
            "1. Choose 3–5 exercises (e.g. sprints, burpees, jumping jacks).\n2. Work at max effort for 20–40 sec.\n3. Rest for 10–20 sec.\n4. Repeat 4–8 rounds with 1–2 min rest between sets."))
        add(exercise("Assault Bike", "Cardio", "Shoulders,Back", "Machine", "Cardio", "Intermediate",
            "1. Adjust seat so legs almost fully extend.\n2. Push/pull handles in sync with legs.\n3. Sprint intervals: 20 sec max effort / 40 sec easy.\n4. Monitor RPM and maintain consistent output."))
        add(exercise("Ski Erg", "Cardio", "Back,Shoulders,Abs/Core", "Machine", "Cardio", "Intermediate",
            "1. Stand in front of machine, grab handles overhead.\n2. Pull handles down in a sweeping motion to hips.\n3. Hinge at hips as handles pass face.\n4. Return hands overhead smoothly and repeat."))
        add(exercise("Shadow Boxing", "Cardio", "Shoulders,Abs/Core", "Bodyweight", "Cardio", "Beginner",
            "1. Stand in boxing stance, fists up.\n2. Throw punches (jab, cross, hook, uppercut) in combinations.\n3. Move feet continuously — don't be flat-footed.\n4. 3 min rounds with 1 min rest."))

        // ═══════════════════ CHEST (fitnessprogramer additions) ═══════════════
        add(exercise("Dumbbell Pullover", "Chest", "Back,Triceps", "Dumbbell", "Isolation", "Beginner",
            "1. Lie perpendicular across a bench, upper back on pad, hips dropped.\n2. Hold dumbbell with both hands above chest.\n3. Lower dumbbell in arc behind head until a stretch is felt.\n4. Pull back to starting position by engaging chest and lats."))
        add(exercise("Barbell Pullover", "Chest", "Back,Triceps", "Barbell", "Isolation", "Intermediate",
            "1. Lie on bench, grip barbell with shoulder-width overhand grip above chest.\n2. Lower bar in arc behind head, feeling the stretch.\n3. Pull bar back to starting position.\n4. Keep a slight bend in elbows throughout."))
        add(exercise("Lying Cable Pullover", "Chest", "Back,Triceps", "Cable", "Isolation", "Beginner",
            "1. Lie on bench in front of low cable, grip bar with arms extended.\n2. Pull bar in arc from overhead down to hips.\n3. Control the return slowly.\n4. Keep arms slightly bent and core tight."))
        add(exercise("Machine Fly", "Chest", "Shoulders", "Machine", "Isolation", "Beginner",
            "1. Sit at fly machine, arms out to sides on pads.\n2. Squeeze arms together in front of chest.\n3. Pause and feel the contraction.\n4. Return slowly, getting a full stretch."))
        add(exercise("Smith Machine Incline Bench Press", "Chest", "Shoulders,Triceps", "Smith Machine", "Push", "Beginner",
            "1. Set bench to 30-45 degrees under Smith machine.\n2. Unrack and lower bar to upper chest.\n3. Press up to lockout.\n4. Re-rack after final rep."))
        add(exercise("Smith Machine Decline Bench Press", "Chest", "Triceps", "Smith Machine", "Push", "Intermediate",
            "1. Set bench to slight decline under Smith machine.\n2. Lie with head at lower end, unrack bar.\n3. Lower bar to lower chest.\n4. Press up to lockout."))
        add(exercise("Lever Chest Press", "Chest", "Shoulders,Triceps", "Machine", "Push", "Beginner",
            "1. Sit at plate-loaded chest press machine, adjust seat height.\n2. Grip handles at chest level.\n3. Press handles forward until arms are extended.\n4. Return slowly with control."))
        add(exercise("High Cable Crossover", "Chest", "Shoulders", "Cable", "Isolation", "Intermediate",
            "1. Set cables at high position on both sides.\n2. Step forward, cables behind and above.\n3. Bring hands down and together in front of hips.\n4. Return slowly with tension throughout."))
        add(exercise("Cable Upper Chest Crossover", "Chest", "Shoulders", "Cable", "Isolation", "Intermediate",
            "1. Set cables at low position on both sides.\n2. Bring hands upward and together at upper chest height.\n3. Focus on squeezing upper chest at the top.\n4. Return slowly."))
        add(exercise("One-Arm Cable Chest Press", "Chest", "Shoulders,Triceps", "Cable", "Push", "Intermediate",
            "1. Stand sideways to cable, set at chest height.\n2. Hold handle in one hand, stagger feet for stability.\n3. Press handle forward and slightly inward.\n4. Return slowly — brace core to resist rotation."))
        add(exercise("Single-Arm Cable Crossover", "Chest", "Shoulders", "Cable", "Isolation", "Intermediate",
            "1. Set cable at high position, stand sideways to machine.\n2. Reach across body and grab handle with far hand.\n3. Pull handle down and across to hip.\n4. Control the return, feeling the chest stretch."))
        add(exercise("Drop Push-Up", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Advanced",
            "1. Start standing on platforms or blocks beside your hands.\n2. Drop to the floor into push-up position.\n3. Perform an explosive push-up.\n4. Return to starting position."))
        add(exercise("Kneeling Push-Up", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Beginner",
            "1. Kneel on floor, hands slightly wider than shoulders.\n2. Lower chest to floor keeping knees as pivot.\n3. Push back up.\n4. Good regression for building toward full push-ups."))
        add(exercise("Parallel Bar Dips", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Intermediate",
            "1. Grip parallel bars, lean torso forward for chest emphasis.\n2. Lower body until upper arms are parallel to floor.\n3. Push back up explosively.\n4. Keep elbows slightly flared."))
        add(exercise("Dips Between Chairs", "Chest", "Shoulders,Triceps", "Bodyweight", "Push", "Beginner",
            "1. Place two sturdy chairs facing each other, hands on seats.\n2. Lean forward and lower body between chairs.\n3. Press back up to start.\n4. Feet can be on floor or elevated."))
        add(exercise("Arm Scissors", "Chest", "Shoulders", "Bodyweight", "Isolation", "Beginner",
            "1. Stand with arms extended in front at shoulder height.\n2. Cross one arm over the other.\n3. Alternate which arm crosses on top.\n4. Move quickly with small range of motion — activates pec minor."))

        // ═══════════════════ BACK (fitnessprogramer additions) ════════════════
        add(exercise("Weighted Pull-Up", "Back", "Biceps", "Other", "Pull", "Advanced",
            "1. Attach weight plate via belt or hold dumbbell between feet.\n2. Grip bar overhand, shoulder-width.\n3. Pull until chin clears bar.\n4. Lower with full control."))
        add(exercise("Muscle-Up", "Back", "Chest,Triceps", "Bodyweight", "Pull", "Advanced",
            "1. Hang from bar with false grip.\n2. Pull explosively, leaning chest toward bar.\n3. Transition elbows over bar quickly.\n4. Press up into dip position to finish."))
        add(exercise("Band Assisted Muscle-Up", "Back", "Chest,Triceps", "Band", "Pull", "Intermediate",
            "1. Loop resistance band over bar, place knees or feet in band.\n2. Pull explosively from hang, leaning chest to bar.\n3. Transition elbows over bar.\n4. Press to lockout."))
        add(exercise("Reverse Lat Pulldown", "Back", "Biceps", "Cable", "Pull", "Beginner",
            "1. Sit at lat pulldown, grip bar with underhand (supinated) grip shoulder-width.\n2. Pull bar down to upper chest.\n3. Squeeze lats and biceps at bottom.\n4. Return slowly."))
        add(exercise("V-Bar Lat Pulldown", "Back", "Biceps", "Cable", "Pull", "Beginner",
            "1. Attach V-bar to lat pulldown cable.\n2. Grip V-bar and sit with knees under pad.\n3. Pull handles down to upper chest.\n4. Squeeze shoulder blades, return slowly."))
        add(exercise("Cable One Arm Lat Pulldown", "Back", "Biceps", "Cable", "Pull", "Intermediate",
            "1. Sit at lat pulldown, grip single handle with one hand.\n2. Pull handle down and across to shoulder.\n3. Lean slightly toward the working side.\n4. Return slowly, feeling full lat stretch."))
        add(exercise("Rope Straight Arm Pulldown", "Back", "Abs/Core", "Cable", "Pull", "Intermediate",
            "1. Set cable high, attach rope, stand facing machine.\n2. Hold rope with straight arms at shoulder height.\n3. Pull rope down to thighs keeping arms straight.\n4. Squeeze lats at bottom, return slowly."))
        add(exercise("Reverse Grip Barbell Row", "Back", "Biceps", "Barbell", "Pull", "Intermediate",
            "1. Stand holding barbell with underhand (supinated) grip.\n2. Hinge forward at hips, back flat.\n3. Row bar to lower chest/upper abdomen.\n4. Lower with control — underhand grip hits lower lats and biceps harder."))
        add(exercise("One-Arm Barbell Row", "Back", "Biceps", "Barbell", "Pull", "Intermediate",
            "1. Stand beside end of barbell on floor.\n2. Hinge over and grip the end with one hand.\n3. Row the end of bar up to hip.\n4. Return slowly, repeat all reps then switch."))
        add(exercise("Incline Barbell Row", "Back", "Biceps", "Barbell", "Pull", "Intermediate",
            "1. Set incline bench to 45 degrees, lie face down.\n2. Grip barbell hanging below bench.\n3. Row bar up toward chest, squeezing shoulder blades.\n4. Lower with control — chest supported removes lower back fatigue."))
        add(exercise("Smith Machine Bent Over Row", "Back", "Biceps", "Smith Machine", "Pull", "Beginner",
            "1. Stand behind Smith machine bar in hinged position.\n2. Unrack bar with overhand grip.\n3. Row bar to lower chest.\n4. Lower with control — fixed bar path helps beginners."))
        add(exercise("Cable Bent Over Row", "Back", "Biceps", "Cable", "Pull", "Beginner",
            "1. Set cable at low position, stand and hinge forward.\n2. Grip bar or handle with both hands.\n3. Row handle to lower chest while staying hinged.\n4. Return slowly maintaining tension."))
        add(exercise("One Arm Cable Row", "Back", "Biceps", "Cable", "Pull", "Beginner",
            "1. Set low pulley, sit or stand facing machine.\n2. Grip handle with one hand, brace other hand on knee.\n3. Row handle to hip, squeezing lat.\n4. Return slowly."))
        add(exercise("Close Grip Cable Row", "Back", "Biceps", "Cable", "Pull", "Beginner",
            "1. Attach close-grip handle to low cable.\n2. Sit upright, feet on platform.\n3. Row handle to lower chest/upper abdomen.\n4. Squeeze shoulder blades, return slowly."))
        add(exercise("Kneeling High Pulley Row", "Back", "Biceps", "Cable", "Pull", "Beginner",
            "1. Set cable at high position, kneel facing machine.\n2. Grip handle with both hands.\n3. Pull to face/neck area, elbows flaring out.\n4. Return slowly — targets upper back and rear delts."))
        add(exercise("Shotgun Row", "Back", "Biceps", "Cable", "Pull", "Intermediate",
            "1. Attach single handle to low cable, stand sideways.\n2. Stagger stance, grip handle, arm extended.\n3. Row handle to hip explosively.\n4. Control return — great for unilateral back training."))
        add(exercise("Ring Inverted Row", "Back", "Biceps", "Bodyweight", "Pull", "Intermediate",
            "1. Set gymnastics rings at waist height.\n2. Grip rings, lean back with body straight.\n3. Pull chest to rings, squeezing shoulder blades.\n4. Lower slowly — rings allow natural wrist rotation."))
        add(exercise("Table Inverted Row", "Back", "Biceps", "Bodyweight", "Beginner", "Beginner",
            "1. Lie under a sturdy table, grip edge with overhand grip.\n2. Keep body straight from head to heels.\n3. Pull chest to table edge.\n4. Lower with control."))
        add(exercise("One Arm Landmine Row", "Back", "Biceps", "Barbell", "Pull", "Intermediate",
            "1. Load one end of barbell in landmine corner.\n2. Stand perpendicular, hinge forward, grip end of bar.\n3. Row bar to hip.\n4. Control the negative."))
        add(exercise("Lever T-Bar Row", "Back", "Biceps", "Machine", "Pull", "Intermediate",
            "1. Stand on lever T-bar machine platform, grip handles.\n2. Hinge forward with chest against pad.\n3. Pull handles to chest.\n4. Return slowly — chest support removes lower back stress."))

        // ═══════════════ SHOULDERS (fitnessprogramer additions) ══════════════
        add(exercise("Handstand Push-Up", "Shoulders", "Triceps", "Bodyweight", "Push", "Advanced",
            "1. Kick up into handstand against wall.\n2. Lower head toward floor by bending elbows.\n3. Press back up to lockout.\n4. Keep core tight and legs together."))
        add(exercise("Scott Press", "Shoulders", "Triceps", "Dumbbell", "Push", "Intermediate",
            "1. Sit holding dumbbells at shoulder level with elbows wide.\n2. Press overhead in an arc, bringing dumbbells together at top.\n3. Lower back to shoulder level.\n4. Combines lateral and overhead press movement."))
        add(exercise("Dumbbell Scaption", "Shoulders", "", "Dumbbell", "Isolation", "Beginner",
            "1. Stand with dumbbells at sides, thumbs up.\n2. Raise arms at 30-45 degree angle forward (scapular plane).\n3. Raise to shoulder height.\n4. Lower slowly — targets supraspinatus and anterior delt."))
        add(exercise("Lateral Raise Machine", "Shoulders", "", "Machine", "Isolation", "Beginner",
            "1. Sit at lateral raise machine, adjust pads to elbows.\n2. Press arms outward against pads.\n3. Raise to shoulder height.\n4. Lower slowly with control."))
        add(exercise("Seated Dumbbell Lateral Raise", "Shoulders", "", "Dumbbell", "Isolation", "Beginner",
            "1. Sit on bench, dumbbells at sides.\n2. Raise arms out to sides.\n3. Stop at shoulder height.\n4. Lower slowly — seated limits cheating."))
        add(exercise("Leaning Cable Lateral Raise", "Shoulders", "", "Cable", "Isolation", "Intermediate",
            "1. Grip low cable, lean away from machine holding support.\n2. Raise arm to side up to shoulder height.\n3. Control return slowly.\n4. Leaning angle increases range of motion at bottom."))
        add(exercise("Incline Dumbbell Reverse Fly", "Shoulders", "Back", "Dumbbell", "Isolation", "Beginner",
            "1. Set incline bench to 45 degrees, lie face down.\n2. Hold dumbbells hanging below chest.\n3. Raise arms out to sides squeezing rear delts.\n4. Lower slowly."))
        add(exercise("Incline Dumbbell Y-Raise", "Shoulders", "Back", "Dumbbell", "Isolation", "Intermediate",
            "1. Lie face down on incline bench, arms hanging.\n2. Raise arms to form a Y shape (thumbs up, 45 degrees above head level).\n3. Squeeze upper back at top.\n4. Lower slowly."))
        add(exercise("Dumbbell Incline T-Raise", "Shoulders", "Back", "Dumbbell", "Isolation", "Intermediate",
            "1. Lie face down on incline bench, arms hanging.\n2. Raise arms out to sides to form a T shape.\n3. Squeeze rear delts and mid-traps.\n4. Lower slowly."))
        add(exercise("Two Arm Dumbbell Front Raise", "Shoulders", "", "Dumbbell", "Isolation", "Beginner",
            "1. Stand holding dumbbells in front of thighs.\n2. Raise both arms simultaneously to shoulder height.\n3. Pause briefly at top.\n4. Lower slowly."))
        add(exercise("Cable Front Raise", "Shoulders", "", "Cable", "Isolation", "Beginner",
            "1. Stand facing away from low cable, hold handle in front.\n2. Raise arm forward to shoulder height.\n3. Pause briefly.\n4. Lower with control."))
        add(exercise("Weight Plate Front Raise", "Shoulders", "", "Other", "Isolation", "Beginner",
            "1. Hold weight plate with both hands at 3 and 9 o'clock.\n2. Raise plate forward to shoulder height.\n3. Pause briefly.\n4. Lower slowly."))
        add(exercise("Dumbbell Cuban Press", "Shoulders", "", "Dumbbell", "Isolation", "Intermediate",
            "1. Hold dumbbells with upright row position.\n2. Pull elbows up to shoulder height.\n3. Rotate forearms up so dumbbells point to ceiling.\n4. Press overhead, then reverse the motion."))
        add(exercise("Band Pull-Apart", "Shoulders", "Back", "Band", "Isolation", "Beginner",
            "1. Hold resistance band in front with overhand grip, arms extended.\n2. Pull band apart by spreading arms.\n3. Bring to chest level.\n4. Return slowly — great for rear delts and rotator cuff."))
        add(exercise("Dumbbell W Press", "Shoulders", "", "Dumbbell", "Isolation", "Intermediate",
            "1. Stand with dumbbells at sides, elbows bent at 90 degrees.\n2. Raise elbows to shoulder height (goalpost position).\n3. Press dumbbells up and slightly in forming a W shape.\n4. Lower to goalpost then return."))
        add(exercise("Seated Behind Neck Press", "Shoulders", "Triceps", "Barbell", "Push", "Advanced",
            "1. Sit with barbell resting on upper traps behind head.\n2. Press barbell overhead to lockout.\n3. Lower behind head.\n4. Requires excellent shoulder mobility — use caution."))
        add(exercise("Push Press", "Shoulders", "Triceps,Quads", "Barbell", "Push", "Intermediate",
            "1. Hold barbell at shoulders.\n2. Dip slightly by bending knees.\n3. Drive upward, using leg power to help press bar overhead.\n4. Lock out, then lower with control."))
        add(exercise("Landmine Squat to Press", "Shoulders", "Quads,Triceps", "Barbell", "Push", "Intermediate",
            "1. Hold end of landmine barbell at chest with both hands.\n2. Squat down.\n3. Drive up from squat and press bar overhead.\n4. Return to starting position."))

        // ═══════════════════ BICEPS (fitnessprogramer additions) ══════════════
        add(exercise("Zottman Curl", "Biceps", "Forearms", "Dumbbell", "Isolation", "Intermediate",
            "1. Hold dumbbells with supinated grip.\n2. Curl to top with supinated grip.\n3. At top, rotate wrists to pronated (overhand) grip.\n4. Lower slowly with pronated grip — hits all heads of biceps and forearms."))
        add(exercise("Waiter Curl", "Biceps", "", "Dumbbell", "Isolation", "Beginner",
            "1. Hold one dumbbell by the upper plate with both palms underneath.\n2. Keep elbows tucked at sides.\n3. Curl dumbbell to shoulder height.\n4. Lower slowly — maximizes long head stretch."))
        add(exercise("Lying High Bench Barbell Curl", "Biceps", "", "Barbell", "Isolation", "Intermediate",
            "1. Lie face up on incline bench set high.\n2. Hold barbell with arms hanging down.\n3. Curl bar to chest.\n4. Lower slowly — increases stretch on the bicep."))
        add(exercise("Cable Incline Biceps Curl", "Biceps", "", "Cable", "Isolation", "Intermediate",
            "1. Lie on incline bench facing away from low cable.\n2. Grip single handles, arms hanging back.\n3. Curl handles to shoulders.\n4. Lower slowly — maximizes long head stretch."))
        add(exercise("Overhead Cable Curl", "Biceps", "", "Cable", "Isolation", "Intermediate",
            "1. Set cables at high position on both sides.\n2. Stand in middle, arms extended overhead gripping handles.\n3. Curl both handles toward temples.\n4. Squeeze hard at peak contraction."))
        add(exercise("Lying Cable Curl", "Biceps", "", "Cable", "Isolation", "Intermediate",
            "1. Lie on floor or bench, low cable behind head.\n2. Grip bar with underhand grip, arms extended.\n3. Curl bar toward forehead.\n4. Return slowly."))
        add(exercise("Dumbbell Reverse Curl", "Biceps", "Forearms", "Dumbbell", "Isolation", "Beginner",
            "1. Hold dumbbells with overhand (pronated) grip.\n2. Curl dumbbells to shoulder height.\n3. Squeeze at top.\n4. Lower slowly — works brachialis and brachioradialis."))
        add(exercise("Close Grip EZ Bar Curl", "Biceps", "Forearms", "EZ Bar", "Isolation", "Beginner",
            "1. Grip EZ bar on inner (narrow) angled sections.\n2. Curl to shoulder height.\n3. Keep elbows stationary.\n4. Lower slowly."))
        add(exercise("Lever Biceps Curl", "Biceps", "", "Machine", "Isolation", "Beginner",
            "1. Sit at plate-loaded bicep curl machine, arms resting on pad.\n2. Grip handles with supinated grip.\n3. Curl to full contraction.\n4. Lower slowly."))
        add(exercise("Biceps Curl Machine", "Biceps", "", "Machine", "Isolation", "Beginner",
            "1. Sit at cable or selectorized bicep curl machine.\n2. Grip handles, elbows on pad.\n3. Curl handles to peak contraction.\n4. Return slowly."))

        // ═══════════════════ TRICEPS (fitnessprogramer additions) ═════════════
        add(exercise("Rope Pushdown", "Triceps", "", "Cable", "Isolation", "Beginner",
            "1. Attach rope to high cable, grip both ends.\n2. Elbows at sides, push rope down.\n3. At bottom, spread rope apart to maximize contraction.\n4. Return slowly."))
        add(exercise("Reverse Grip Pushdown", "Triceps", "", "Cable", "Isolation", "Beginner",
            "1. Attach bar to high cable, grip with underhand (supinated) grip.\n2. Elbows at sides, push bar down.\n3. Extend fully, feeling long head contraction.\n4. Return slowly."))
        add(exercise("Dumbbell Skull Crusher", "Triceps", "", "Dumbbell", "Isolation", "Intermediate",
            "1. Lie on bench, hold dumbbells with palms facing each other above chest.\n2. Lower dumbbells toward temples by bending elbows.\n3. Keep upper arms perpendicular to floor.\n4. Extend back up."))
        add(exercise("Seated Dumbbell Triceps Extension", "Triceps", "", "Dumbbell", "Isolation", "Beginner",
            "1. Sit upright, hold one dumbbell overhead with both hands.\n2. Lower dumbbell behind head by bending elbows.\n3. Keep upper arms close to head.\n4. Extend back up."))
        add(exercise("Seated One-Arm Dumbbell Triceps Extension", "Triceps", "", "Dumbbell", "Isolation", "Beginner",
            "1. Sit upright, hold one dumbbell overhead with one hand.\n2. Lower behind head.\n3. Keep upper arm close to ear.\n4. Extend back up, complete all reps then switch."))
        add(exercise("Kneeling Cable Triceps Extension", "Triceps", "", "Cable", "Isolation", "Beginner",
            "1. Kneel facing a high cable, grip rope or bar overhead.\n2. Keep elbows by ears.\n3. Extend arms forward and downward.\n4. Return slowly."))
        add(exercise("Cable Lying Triceps Extension", "Triceps", "", "Cable", "Isolation", "Intermediate",
            "1. Lie on bench with head near low cable.\n2. Grip bar with overhand grip, arms extended toward cable.\n3. Extend arms away from cable pulling bar over head.\n4. Return slowly — constant tension throughout."))
        add(exercise("High Pulley Overhead Triceps Extension", "Triceps", "", "Cable", "Isolation", "Beginner",
            "1. Set cable at high position, grip rope with both hands.\n2. Face away from machine, elbows bent by ears.\n3. Extend arms forward away from machine.\n4. Return slowly."))
        add(exercise("Cable Side Triceps Extension", "Triceps", "", "Cable", "Isolation", "Intermediate",
            "1. Stand sideways to cable, grip single handle at shoulder height.\n2. Hold upper arm parallel to floor, elbow at 90 degrees.\n3. Extend forearm out, fully locking elbow.\n4. Return slowly."))
        add(exercise("Seated EZ-Bar Overhead Triceps Extension", "Triceps", "", "EZ Bar", "Isolation", "Intermediate",
            "1. Sit upright, hold EZ bar overhead with narrow grip.\n2. Lower bar behind head by bending elbows.\n3. Keep upper arms close to head throughout.\n4. Extend back to start."))
        add(exercise("Cross Arm Push-Up", "Triceps", "Chest", "Bodyweight", "Push", "Intermediate",
            "1. Start in push-up position.\n2. Place one hand across the centerline under chest, other arm normal.\n3. Lower and press up.\n4. Alternate hand placement each rep."))

        // ═══════════════════ LEGS (fitnessprogramer additions) ════════════════
        add(exercise("Barbell Lunge", "Quads", "Glutes,Hamstrings", "Barbell", "Squat", "Intermediate",
            "1. Rest barbell on upper back.\n2. Step forward into lunge, back knee approaches floor.\n3. Drive through front heel to return.\n4. Alternate legs each rep."))
        add(exercise("Side Lunge", "Quads", "Glutes,Hamstrings", "Bodyweight", "Squat", "Beginner",
            "1. Stand with feet together.\n2. Step wide to one side, bending that knee.\n3. Keep opposite leg straight.\n4. Push back to center and repeat on other side."))
        add(exercise("Curtsy Lunge", "Quads", "Glutes", "Bodyweight", "Squat", "Beginner",
            "1. Stand with feet hip-width.\n2. Step one foot behind and across the other leg.\n3. Lower into lunge position.\n4. Return to standing, alternate sides."))
        add(exercise("Barbell Bulgarian Split Squat", "Quads", "Glutes", "Barbell", "Squat", "Advanced",
            "1. Rest barbell on upper back, rear foot on bench.\n2. Lower into lunge, front thigh parallel to floor.\n3. Drive through front heel to stand.\n4. Complete all reps before switching legs."))
        add(exercise("Barbell Hack Squat", "Quads", "Glutes", "Barbell", "Squat", "Intermediate",
            "1. Stand with barbell behind legs, grip overhand behind hips.\n2. Squat down keeping chest up.\n3. Drive through heels to stand.\n4. Historically the original hack squat."))
        add(exercise("Bodyweight Squat", "Quads", "Glutes,Hamstrings", "Bodyweight", "Squat", "Beginner",
            "1. Stand with feet shoulder-width, toes slightly out.\n2. Sit back and down, keeping chest tall.\n3. Lower until thighs parallel to floor.\n4. Drive through heels to stand."))
        add(exercise("Bodyweight Sumo Squat", "Quads", "Glutes", "Bodyweight", "Squat", "Beginner",
            "1. Stand with feet wide, toes pointed 45 degrees out.\n2. Lower into squat keeping knees tracking toes.\n3. Drive through heels to stand.\n4. Squeeze glutes at top."))
        add(exercise("Cossack Squat", "Quads", "Glutes,Hamstrings", "Bodyweight", "Squat", "Advanced",
            "1. Stand with feet wide.\n2. Shift weight to one side, bending that knee into a deep squat.\n3. Other leg stays straight, foot flat or heel only.\n4. Return through center and shift to opposite side."))
        add(exercise("Jump Squats", "Quads", "Glutes,Calves", "Bodyweight", "Squat", "Intermediate",
            "1. Stand with feet shoulder-width.\n2. Squat down to parallel.\n3. Explode upward, leaving the floor.\n4. Land softly with bent knees and immediately begin next rep."))
        add(exercise("Dumbbell Cossack Squat", "Quads", "Glutes,Hamstrings", "Dumbbell", "Squat", "Intermediate",
            "1. Hold dumbbell at chest, stand with feet wide.\n2. Shift into deep lateral squat on one side.\n3. Opposite leg remains straight.\n4. Return and shift to other side."))
        add(exercise("Heel-Elevated Goblet Squat", "Quads", "Glutes", "Dumbbell", "Squat", "Beginner",
            "1. Place heels on small plates or wedge, hold dumbbell at chest.\n2. Squat deep — heel elevation increases quad bias.\n3. Drive through heels to stand.\n4. Keep knees tracking toes."))
        add(exercise("Pendulum Lunge", "Quads", "Glutes,Hamstrings", "Bodyweight", "Squat", "Intermediate",
            "1. Step forward into forward lunge.\n2. Without returning foot to center, swing it directly into reverse lunge.\n3. One fluid pendulum motion per rep.\n4. Complete all reps on one leg before switching."))
        add(exercise("Barbell Lateral Lunge", "Quads", "Glutes,Hamstrings", "Barbell", "Squat", "Advanced",
            "1. Rest barbell on upper back.\n2. Step wide to one side, bending that knee.\n3. Keep opposite leg straight.\n4. Push back to center."))
        add(exercise("Dumbbell Rear Lunge", "Quads", "Glutes,Hamstrings", "Dumbbell", "Squat", "Beginner",
            "1. Hold dumbbells at sides.\n2. Step backward into reverse lunge.\n3. Lower rear knee toward floor.\n4. Drive through front heel to return."))
        add(exercise("Static Lunge", "Quads", "Glutes,Hamstrings", "Bodyweight", "Squat", "Beginner",
            "1. Step one foot forward into lunge position and hold.\n2. Lower rear knee toward floor.\n3. Drive back up without moving feet.\n4. Complete all reps then switch legs."))
        add(exercise("Lever Hip Abduction", "Glutes", "", "Machine", "Isolation", "Beginner",
            "1. Sit at hip abduction machine, pads against outer thighs.\n2. Push legs outward against pads.\n3. Squeeze outer glutes at full extension.\n4. Return slowly with control."))
        add(exercise("Cable Hip Adduction", "Glutes", "Quads", "Cable", "Isolation", "Beginner",
            "1. Attach ankle cuff to low cable, stand sideways.\n2. Pull working leg across and in front of standing leg.\n3. Control the return.\n4. Focus on inner thigh contraction."))

        // ═══════════════════ CALVES (fitnessprogramer additions) ══════════════
        add(exercise("Standing Barbell Calf Raise", "Calves", "", "Barbell", "Isolation", "Beginner",
            "1. Place barbell across upper back, stand on edge of step or plates.\n2. Rise on toes fully.\n3. Squeeze calves at top.\n4. Lower heel below platform for full stretch."))
        add(exercise("Barbell Seated Calf Raise", "Calves", "", "Barbell", "Isolation", "Beginner",
            "1. Sit on bench, place barbell across thighs near knees (use padding).\n2. Rise up on toes.\n3. Squeeze at top.\n4. Lower for full stretch."))
        add(exercise("Weighted Seated Calf Raise", "Calves", "", "Other", "Isolation", "Beginner",
            "1. Sit on bench or machine, place weight on thighs.\n2. Rise up on toes.\n3. Squeeze calves at top.\n4. Full stretch at bottom."))
        add(exercise("Squat Hold Calf Raise", "Calves", "Quads", "Bodyweight", "Isolation", "Intermediate",
            "1. Hold bottom of squat position.\n2. Rise up on toes while in squat.\n3. Lower heels back to floor.\n4. Combine calf and quad work."))
        add(exercise("Partner Donkey Calf Raise", "Calves", "", "Bodyweight", "Isolation", "Intermediate",
            "1. Lean forward on a bench or rack, partner sits on your lower back.\n2. Stand on edge of step for range of motion.\n3. Rise on toes against the partner's weight.\n4. Lower for full stretch."))

        // ═══════════════════ CORE/ABS (fitnessprogramer additions) ════════════
        add(exercise("Weighted Crunch", "Abs/Core", "", "Other", "Isolation", "Beginner",
            "1. Lie on back, hold weight plate on chest.\n2. Curl shoulders off floor.\n3. Squeeze abs at top.\n4. Lower slowly."))
        add(exercise("Kneeling Cable Crunch", "Abs/Core", "", "Cable", "Isolation", "Beginner",
            "1. Kneel facing high cable, grip rope behind neck.\n2. Crunch forward toward knees.\n3. Squeeze abs hard at bottom.\n4. Return to upright slowly."))
        add(exercise("T-Cross Sit-Up", "Abs/Core", "", "Bodyweight", "Isolation", "Intermediate",
            "1. Lie on back, arms out in T position.\n2. Sit up and reach one hand to opposite foot.\n3. Lower back down.\n4. Alternate sides each rep."))
        add(exercise("Tuck Crunch", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Lie on back, knees tucked to chest.\n2. Simultaneously curl shoulders up while pulling knees in.\n3. Squeeze at peak contraction.\n4. Extend and repeat."))
        add(exercise("Ab Roller Crunch", "Abs/Core", "", "Other", "Isolation", "Intermediate",
            "1. Kneel with ab roller, elbows slightly bent.\n2. Roll forward as far as possible while maintaining control.\n3. Pull back using abs.\n4. Keep lower back from sagging."))
        add(exercise("Toes to Bar", "Abs/Core", "Forearms", "Bodyweight", "Isolation", "Advanced",
            "1. Hang from pull-up bar, arms extended.\n2. Keep legs straight.\n3. Raise feet up to touch the bar.\n4. Lower with control."))
        add(exercise("Captain's Chair Leg Raise", "Abs/Core", "", "Bodyweight", "Isolation", "Intermediate",
            "1. Sit in captain's chair, forearms on pads.\n2. Keep back against pad.\n3. Raise knees to chest or legs to parallel.\n4. Lower slowly."))
        add(exercise("Alternate Leg Raises", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Lie flat on back, hands under hips.\n2. Raise one leg to 90 degrees.\n3. Lower it as you raise the other.\n4. Alternate in a controlled scissoring motion."))
        add(exercise("Lying Scissor Kicks", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Lie on back, legs raised slightly off floor.\n2. Raise one leg higher while lowering the other.\n3. Alternate in a scissors motion.\n4. Keep lower back pressed to floor."))
        add(exercise("Dead Bug", "Abs/Core", "", "Bodyweight", "Isolation", "Beginner",
            "1. Lie on back, arms extended toward ceiling, knees at 90 degrees.\n2. Slowly lower opposite arm and leg toward floor.\n3. Keep lower back pressed to floor throughout.\n4. Return and repeat on opposite side."))
        add(exercise("L-Sit", "Abs/Core", "Triceps", "Bodyweight", "Isolation", "Advanced",
            "1. Support yourself on parallel bars or dip station.\n2. Lift legs parallel to floor, forming an L shape.\n3. Hold the position.\n4. Keep legs straight and core tight."))
        add(exercise("Heel Touch", "Abs/Core", "Obliques", "Bodyweight", "Isolation", "Beginner",
            "1. Lie on back, knees bent, feet flat on floor.\n2. Reach one hand down to touch the same-side heel.\n3. Return and alternate sides.\n4. Crunch laterally to work obliques."))
        add(exercise("Cable Side Bend", "Abs/Core", "Obliques", "Cable", "Isolation", "Beginner",
            "1. Stand sideways to low cable, grip handle in one hand.\n2. Bend laterally away from cable.\n3. Return upright against resistance.\n4. Complete all reps then switch sides."))
        add(exercise("Barbell Side Bend", "Abs/Core", "Obliques", "Barbell", "Isolation", "Intermediate",
            "1. Stand with barbell on upper back.\n2. Bend laterally to one side.\n3. Return to upright.\n4. Alternate sides — keep hips level, don't twist."))
        add(exercise("Seated Oblique Twist", "Abs/Core", "Obliques", "Bodyweight", "Isolation", "Beginner",
            "1. Sit on floor, knees bent, lean back slightly.\n2. Clasp hands or hold weight.\n3. Rotate torso side to side.\n4. Focus on oblique contraction."))
        add(exercise("Front to Side Plank", "Abs/Core", "Obliques", "Bodyweight", "Isolation", "Intermediate",
            "1. Start in front plank on forearms.\n2. Rotate to side plank on one forearm.\n3. Hold briefly.\n4. Return to front plank and rotate other side."))
        add(exercise("Stability Ball Knee Tuck", "Abs/Core", "", "Other", "Isolation", "Intermediate",
            "1. Start in push-up position with feet on stability ball.\n2. Draw knees toward chest, rolling ball forward.\n3. Extend back to start.\n4. Keep hips level throughout."))
        add(exercise("Dumbbell V-Up", "Abs/Core", "", "Dumbbell", "Isolation", "Intermediate",
            "1. Lie flat, hold dumbbell above chest.\n2. Simultaneously raise legs and torso.\n3. Reach dumbbell toward feet at top.\n4. Lower slowly with control."))
        add(exercise("Kettlebell Windmill", "Abs/Core", "Shoulders,Hamstrings", "Kettlebell", "Isolation", "Advanced",
            "1. Press kettlebell overhead with one arm.\n2. Push hip out to kettlebell side, bend laterally.\n3. Lower free hand down leg toward floor.\n4. Return to upright — demands shoulder stability and hip mobility."))
        add(exercise("Standing Cable Twist", "Abs/Core", "Obliques", "Cable", "Isolation", "Beginner",
            "1. Set cable at chest height, stand sideways.\n2. Hold handle with both hands, arms extended.\n3. Rotate through core away from the machine.\n4. Control the return."))

        // ══════════════════ FOREARMS (fitnessprogramer additions) ═════════════
        add(exercise("Barbell Reverse Curl", "Forearms", "Biceps", "Barbell", "Isolation", "Beginner",
            "1. Stand with barbell using overhand (pronated) grip.\n2. Curl bar to shoulder height.\n3. Keep elbows stationary.\n4. Lower slowly — works brachioradialis and wrist extensors."))
        add(exercise("Dumbbell Finger Curl", "Forearms", "", "Dumbbell", "Isolation", "Beginner",
            "1. Sit with forearms on thighs, hold dumbbells.\n2. Let weights roll to fingertips.\n3. Curl fingers to full grip.\n4. Lower again — isolated finger flexor work."))
        add(exercise("Barbell Finger Curl", "Forearms", "", "Barbell", "Isolation", "Beginner",
            "1. Sit with forearms on thighs, hold barbell.\n2. Allow bar to roll to fingertips.\n3. Curl back to full grip.\n4. Strengthens finger flexors and grip."))
        add(exercise("Wrist Roller", "Forearms", "", "Other", "Isolation", "Intermediate",
            "1. Hold wrist roller at arm's length (parallel to floor).\n2. Roll weight up by alternately curling wrists.\n3. Once weight reaches top, slowly unroll.\n4. Works both flexors and extensors."))
        add(exercise("Behind The Back Wrist Curl", "Forearms", "", "Barbell", "Isolation", "Beginner",
            "1. Stand holding barbell behind back with overhand grip.\n2. Let bar roll to fingertips.\n3. Curl wrists up.\n4. Lower slowly."))
        add(exercise("Barbell Reverse Wrist Curl", "Forearms", "", "Barbell", "Isolation", "Beginner",
            "1. Sit with forearms on thighs, hold barbell with overhand grip.\n2. Let wrists flex down.\n3. Extend wrists upward.\n4. Lower slowly — works wrist extensors."))
        add(exercise("Hammer Curl with Band", "Biceps", "Forearms", "Band", "Isolation", "Beginner",
            "1. Stand on resistance band, hold handles with neutral (hammer) grip.\n2. Curl handles to shoulder height.\n3. Keep palms facing each other throughout.\n4. Lower slowly."))

        // ══════════════════ TRAPS (fitnessprogramer additions) ════════════════
        add(exercise("Cable Shrug", "Traps", "", "Cable", "Isolation", "Beginner",
            "1. Stand facing cable machine, hold bar at thigh level.\n2. Shrug shoulders straight up.\n3. Hold briefly at top.\n4. Lower slowly."))
        add(exercise("Smith Machine Shrug", "Traps", "", "Smith Machine", "Isolation", "Beginner",
            "1. Stand inside Smith machine, grip bar at thigh height.\n2. Shrug shoulders upward.\n3. Pause at top.\n4. Lower slowly — heavier loads possible than dumbbell shrug."))
        add(exercise("Lever Shrug", "Traps", "", "Machine", "Isolation", "Beginner",
            "1. Grip plate-loaded shrug machine handles.\n2. Shrug shoulders upward as high as possible.\n3. Pause at top.\n4. Lower slowly."))
        add(exercise("Barbell Rear Delt Raise", "Traps", "Shoulders", "Barbell", "Isolation", "Intermediate",
            "1. Lie face down on incline bench, hold barbell below.\n2. Raise bar out to sides, elbows slightly bent.\n3. Squeeze rear delts and traps at top.\n4. Lower slowly."))
        add(exercise("Bent Over Reverse Cable Fly", "Traps", "Shoulders", "Cable", "Isolation", "Beginner",
            "1. Set cables at low position, cross arms to grab opposite handles.\n2. Hinge forward, raise arms out to sides uncrossing them.\n3. Squeeze rear delts at top.\n4. Return slowly."))
    }

    private fun exercise(
        name: String,
        primary: String,
        secondary: String,
        equipment: String,
        movement: String,
        difficulty: String,
        instructions: String
    ) = ExerciseEntity(
        name = name,
        primaryMuscleGroup = primary,
        secondaryMuscleGroups = secondary,
        equipmentType = equipment,
        movementType = movement,
        difficulty = difficulty,
        instructions = instructions,
        imageResName = name.lowercase().replace(" ", "_").replace("(", "").replace(")", "").replace("-", "_"),
        isCustom = false
    )
}
