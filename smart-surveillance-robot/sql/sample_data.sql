-- Optional demo data. Run AFTER the application has started once
-- (the app creates the default robot SPSR-01 and the admin/operator users).
USE surveillance_robot;

SET @rid = (SELECT id FROM robots WHERE robot_code = 'SPSR-01');

INSERT INTO sensor_readings (robot_id, ultrasonic_cm, ir_triggered, sound_triggered, recorded_at) VALUES
  (@rid, 42.50, 0, 0, NOW(6) - INTERVAL 5 MINUTE),
  (@rid, 18.20, 1, 0, NOW(6) - INTERVAL 4 MINUTE),
  (@rid,  3.90, 0, 1, NOW(6) - INTERVAL 3 MINUTE);

INSERT INTO detection_events (robot_id, object_class, confidence, latitude, longitude, alert_sent, detected_at) VALUES
  (@rid, 'person', 0.87, 17.5449, 78.5718, 1, NOW(6) - INTERVAL 2 MINUTE),
  (@rid, 'person', 0.64, 17.5450, 78.5720, 0, NOW(6) - INTERVAL 1 MINUTE);

INSERT INTO alerts (robot_id, type, message, delivered, acknowledged, created_at) VALUES
  (@rid, 'DETECTION', 'ALERT [SPSR-01] person detected (confidence 87%) at location 17.5449, 78.5718', 1, 0, NOW(6) - INTERVAL 2 MINUTE);
