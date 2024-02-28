# ** This file is not used in the main Java Project **

# The file is provided here so that it can be copied and ran independently on a raspberry pi to listen
# to connections on http://raspberrypi.local:5000 and return images from it's camera module.
# See java.uk.ac.nottingham.hybridarcade.hardware.scanner for it's use by the client.

import time
import io
from flask import Flask, send_file
from picamera2 import Picamera2, Preview
from libcamera import Transform

app = Flask(__name__)

picam = Picamera2()
config = picam.create_still_configuration({
    "format"    : "BGR888",
    "size"      : (1080, 1080),
    },
    transform   = Transform(hflip=1, vflip=1)
    )
picam.configure(config)

@app.route("/")
def send():
    picam.start()
    time.sleep(2)
    picam.capture_file('last.png')
    picam.stop()
    return send_file('last.png', mimetype='image/png')
