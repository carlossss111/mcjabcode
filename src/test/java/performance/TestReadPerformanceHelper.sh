#!/bin/bash
#cd "../..";
#TEST_ROOT="$(pwd)"
TEST_ROOT="src/test"
RESOURCES_DIR="$TEST_ROOT/resources/performancetest"

image_modify() {
    for i in $(seq 0 -5 -100); do
        convert -brightness-contrast "0 x$i" "$RESOURCES_DIR/sample_barcode.png" "$RESOURCES_DIR/contrast$i.png"
        convert -brightness-contrast "$i x0" "$RESOURCES_DIR/sample_barcode.png" "$RESOURCES_DIR/brightness$i.png"
    done
    for i in $(seq 0 1 10); do
        convert -blur "4 x$i" "$RESOURCES_DIR/sample_barcode.png" "$RESOURCES_DIR/blur$i.png"
    done
    for i in $(seq 0 5 90); do
        convert -rotate "$i" "$RESOURCES_DIR/sample_barcode.png" "$RESOURCES_DIR/rotate$i.png"
    done
    for i in $(seq 0 10 300); do
        convert -virtual-pixel white -distort Perspective "0,732,0,732  732,732,732,$((732-i))  732,0,732,$i  0,0,0,0" "$RESOURCES_DIR/sample_barcode.png" "$RESOURCES_DIR/perspective$i.png"
    done
}

image_cleanup() {
    rm $RESOURCES_DIR/contrast*.png
    rm $RESOURCES_DIR/brightness*.png
    rm $RESOURCES_DIR/blur*.png
    rm $RESOURCES_DIR/rotate*.png
    rm $RESOURCES_DIR/perspective*.png
}

if [ "$1" = 'modify' ]; then
    image_modify
elif [ "$1" = 'clean' ]; then
    image_cleanup
fi

exit 0
