package name.modid;

enum LanturnColor {
	WHITE("white", 0xF9FFFE),
	ORANGE("orange", 0xF9801D),
	MAGENTA("magenta", 0xC74EBD),
	LIGHT_BLUE("light_blue", 0x3AB3DA),
	YELLOW("yellow", 0xFED83D),
	LIME("lime", 0x80C71F),
	PINK("pink", 0xF38BAA),
	GRAY("gray", 0x474F52),
	LIGHT_GRAY("light_gray", 0x9D9D97),
	CYAN("cyan", 0x169C9C),
	PURPLE("purple", 0x8932B8),
	BLUE("blue", 0x3C44AA),
	BROWN("brown", 0x835432),
	GREEN("green", 0x5E7C16),
	RED("red", 0xB02E26),
	BLACK("black", 0x1D1D21);

	private final String id;
	private final int rgb;

	LanturnColor(String id, int rgb) {
		this.id = id;
		this.rgb = rgb;
	}

	String id() {
		return id;
	}

	int rgb() {
		return rgb;
	}

	String lanternId() {
		return this == WHITE ? "lantern" : "lantern_" + id;
	}

	String lightId() {
		return this == WHITE ? "fading_light" : "fading_light_" + id;
	}

	static LanturnColor byId(String id) {
		for (LanturnColor color : values()) {
			if (color.id.equals(id)) {
				return color;
			}
		}
		return WHITE;
	}
}
