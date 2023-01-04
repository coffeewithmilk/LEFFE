package view;

import java.awt.event.KeyEvent;

public class KeyAdapter {

	private String key;
	private int kc;
	
	private KeyAdapter(String key, int kc) {
		this.key = key;
		this.kc = kc;
	}
	

	public static void main(String[] args) {
		for (KeyAdapter k : KEY_ADAPTERS) {
			System.out.println("<div class=\"slideargument\">" + k.key + "</div>");
		}
	}
	
	private static final KeyAdapter[] KEY_ADAPTERS = { new KeyAdapter("A", KeyEvent.VK_A),
			new KeyAdapter("B", KeyEvent.VK_B), new KeyAdapter("C", KeyEvent.VK_C), new KeyAdapter("D", KeyEvent.VK_D),
			new KeyAdapter("E", KeyEvent.VK_E), new KeyAdapter("F", KeyEvent.VK_F), new KeyAdapter("G", KeyEvent.VK_G),
			new KeyAdapter("H", KeyEvent.VK_H), new KeyAdapter("I", KeyEvent.VK_I), new KeyAdapter("J", KeyEvent.VK_J),
			new KeyAdapter("K", KeyEvent.VK_K), new KeyAdapter("L", KeyEvent.VK_L), new KeyAdapter("M", KeyEvent.VK_M),
			new KeyAdapter("N", KeyEvent.VK_N), new KeyAdapter("O", KeyEvent.VK_O), new KeyAdapter("P", KeyEvent.VK_P),
			new KeyAdapter("Q", KeyEvent.VK_Q), new KeyAdapter("R", KeyEvent.VK_R), new KeyAdapter("S", KeyEvent.VK_S),
			new KeyAdapter("T", KeyEvent.VK_T), new KeyAdapter("U", KeyEvent.VK_U), new KeyAdapter("V", KeyEvent.VK_V),
			new KeyAdapter("W", KeyEvent.VK_W), new KeyAdapter("X", KeyEvent.VK_X), new KeyAdapter("Y", KeyEvent.VK_Y),
			new KeyAdapter("Z", KeyEvent.VK_Z), new KeyAdapter("1", KeyEvent.VK_1), new KeyAdapter("2", KeyEvent.VK_2),
			new KeyAdapter("3", KeyEvent.VK_3), new KeyAdapter("4", KeyEvent.VK_4), new KeyAdapter("5", KeyEvent.VK_5),
			new KeyAdapter("6", KeyEvent.VK_6), new KeyAdapter("7", KeyEvent.VK_7), new KeyAdapter("8", KeyEvent.VK_8),
			new KeyAdapter("9", KeyEvent.VK_9), new KeyAdapter("0", KeyEvent.VK_0),
			new KeyAdapter("SHIFT", KeyEvent.VK_SHIFT), 
			new KeyAdapter("CAPS", KeyEvent.VK_CAPS_LOCK), new KeyAdapter("TAB", KeyEvent.VK_TAB),
			new KeyAdapter("SPACE", KeyEvent.VK_SPACE), new KeyAdapter("F1", KeyEvent.VK_F1),
			new KeyAdapter("F2", KeyEvent.VK_F2), new KeyAdapter("F3", KeyEvent.VK_F3),
			new KeyAdapter("F4", KeyEvent.VK_F4), new KeyAdapter("F5", KeyEvent.VK_F5),
			new KeyAdapter("F6", KeyEvent.VK_F6), new KeyAdapter("F7", KeyEvent.VK_F7),
			new KeyAdapter("F8", KeyEvent.VK_F8), new KeyAdapter("F9", KeyEvent.VK_F9),
			new KeyAdapter("F10", KeyEvent.VK_F10), new KeyAdapter("F11", KeyEvent.VK_F11),
			new KeyAdapter("F12", KeyEvent.VK_F12), new KeyAdapter("BACKSPACE", KeyEvent.VK_BACK_SPACE),
			new KeyAdapter("ESCAPE", KeyEvent.VK_ESCAPE), 
			new KeyAdapter("DOWN", KeyEvent.VK_DOWN), 
			new KeyAdapter("RIGHT", KeyEvent.VK_RIGHT), 
			new KeyAdapter("UP", KeyEvent.VK_UP), 
			new KeyAdapter("LEFT", KeyEvent.VK_LEFT), new KeyAdapter("HOME", KeyEvent.VK_HOME),
			new KeyAdapter("END", KeyEvent.VK_END),
			new KeyAdapter("DEL", KeyEvent.VK_DELETE),
			new KeyAdapter("PLUS", KeyEvent.VK_PLUS),
			new KeyAdapter("MINUS", KeyEvent.VK_MINUS),
			new KeyAdapter("WIN", KeyEvent.VK_WINDOWS),
			new KeyAdapter("CTRL", KeyEvent.VK_CONTROL),
			new KeyAdapter("ENTER", KeyEvent.VK_ENTER),
			new KeyAdapter("ALT", KeyEvent.VK_ALT),
	};

	public static int kc(String key) {
		for (int i = 0; i < KEY_ADAPTERS.length; i++) {
			KeyAdapter ka = KEY_ADAPTERS[i];
			if (ka.key.equals(key)) {
				return ka.kc;
			}
		}
		return -1;
	}

}
