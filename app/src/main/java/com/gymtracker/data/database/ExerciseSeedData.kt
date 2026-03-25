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
