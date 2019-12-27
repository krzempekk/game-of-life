public enum BoardDirection {
    NORTH(new Vector2D(0, 1)),
    NORTHEAST(new Vector2D(1, 1)),
    EAST(new Vector2D(1, 0)),
    SOUTHEAST(new Vector2D(1, -1)),
    SOUTH(new Vector2D(0, -1)),
    SOUTHWEST(new Vector2D(-1, -1)),
    WEST(new Vector2D(-1, 0)),
    NORTHWEST(new Vector2D(-1, 1));

    private Vector2D unitVector;

    BoardDirection(Vector2D unitVector) {
        this.unitVector = unitVector;
    }

    public Vector2D getUnitVector() {
        return this.unitVector;
    }
}