package Pacman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PacMan extends JPanel implements ActionListener, KeyListener {

	private int rowCount = 21;
	private int columnCount = 19;
	private int tileSize = 32;
	private int boardWidth = columnCount * tileSize;
	private int boardHeight = rowCount * tileSize;

	private Image wallImage;
	private Image blueGhostImage;
	private Image orangeGhostImage;
	private Image pinkGhostImage;
	private Image redGhostImage;

	private Image pacmanUpImage;
	private Image pacmanDownImage;
	private Image pacmanLeftImage;
	private Image pacmanRightImage;

	HashSet<Block> walls;
	HashSet<Block> foods;
	HashSet<Block> ghosts;
	Block pacman;

	Timer gameLoop;
	char[] directions = { 'U', 'D', 'L', 'R' };
	Random random = new Random();
	int score = 0;
	int lives = 3;
	boolean gameOver = false;

	PacMan() {
		setPreferredSize(new Dimension(boardWidth, boardHeight));
		setBackground(Color.BLACK);
		addKeyListener(this);
		setFocusable(true);

		wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
		blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
		orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
		pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
		redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

		pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
		pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
		pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
		pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

		loadMap();

		for (Block ghost : ghosts) {
			char newDirection = directions[random.nextInt(4)];
			ghost.updateDirection(newDirection, tileSize, walls, this);
		}

		gameLoop = new Timer(50, this);
		gameLoop.start();
	}

	public void loadMap() {
		walls = new HashSet<>();
		foods = new HashSet<>();
		ghosts = new HashSet<>();

		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				char tileMapChar = Map.tileMap[r].charAt(c);

				int x = c * tileSize;
				int y = r * tileSize;

				if (tileMapChar == 'X') {
					walls.add(new Block(wallImage, x, y, tileSize, tileSize));
				} else if (tileMapChar == 'b') {
					ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
				} else if (tileMapChar == 'o') {
					ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
				} else if (tileMapChar == 'p') {
					ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
				} else if (tileMapChar == 'r') {
					ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
				} else if (tileMapChar == 'P') {
					pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
				} else if (tileMapChar == ' ') {
					foods.add(new Block(null, x + 14, y + 14, 4, 4));
				}
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void draw(Graphics g) {
		g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

		for (Block ghost : ghosts) {
			g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
		}

		for (Block wall : walls) {
			g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
		}

		g.setColor(Color.WHITE);
		for (Block food : foods) {
			g.fillRect(food.x, food.y, food.width, food.height);
		}
		// score
		g.setFont(new Font("Arial", Font.PLAIN, 18));
		if (gameOver) {
			g.drawString("Game Over: " + String.valueOf(score), tileSize / 2, tileSize / 2);
		} else {
			g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize / 2, tileSize / 2);
		}
	}

	public void move() {
		pacman.x += pacman.velocityX;
		pacman.y += pacman.velocityY;

		if (pacman.x < 0) {
			pacman.x = 0;
		} else if (pacman.x + pacman.width > boardWidth) {
			pacman.x = boardWidth - pacman.width;
		}

		if (pacman.y < 0) {
			pacman.y = 0;
		} else if (pacman.y + pacman.height > boardHeight) {
			pacman.y = boardHeight - pacman.height;
		}

		// check wall collisions
		for (Block wall : walls) {
			if (collision(pacman, wall)) {
				pacman.x -= pacman.velocityX;
				pacman.y -= pacman.velocityY;
				break;
			}
		}
		// check ghost collisions
		for (Block ghost : ghosts) {
			if (collision(ghost, pacman)) {
				lives--;

				if (lives == 0) {
					gameOver = true;
					return;
				}

				resetPositions();
			}

			if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
				ghost.updateDirection('U', tileSize, walls, this);
			}
			ghost.x += ghost.velocityX;
			ghost.y += ghost.velocityY;

			for (Block wall : walls) {
				if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {

					ghost.x -= ghost.velocityX;
					ghost.y -= ghost.velocityY;

					char newDirection = directions[random.nextInt(4)];
					ghost.updateDirection(newDirection, tileSize, walls, this);

					break;
				}
			}
		}

		Block foodEaten = null;

		for (Block food : foods) {
			if (collision(pacman, food)) {
				foodEaten = food;
				score += 10;
			}
		}

		if (foodEaten != null) {
			foods.remove(foodEaten);
		}

		if (foods.isEmpty()) {
			loadMap();
			resetPositions();
		}
	}

	public boolean collision(Block a, Block b) {
		return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
	}

	public void resetPositions() {

		pacman.reset();
		pacman.velocityX = 0;
		pacman.velocityY = 0;

		for (Block ghost : ghosts) {
			ghost.reset();

			char newDirection = directions[random.nextInt(4)];
			ghost.updateDirection(newDirection, tileSize, walls, this);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		move();
		repaint();
		if (gameOver) {
			gameLoop.stop();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

		if (gameOver) {
			loadMap();
			resetPositions();
			lives = 3;
			score = 0;
			gameOver = false;
			gameLoop.start();
		}

		if (e.getKeyCode() == KeyEvent.VK_UP) {
			pacman.updateDirection('U', tileSize, walls, this);

		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			pacman.updateDirection('D', tileSize, walls, this);

		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			pacman.updateDirection('L', tileSize, walls, this);

		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			pacman.updateDirection('R', tileSize, walls, this);
		}

		if (pacman.direction == 'U') {
			pacman.image = pacmanUpImage;

		} else if (pacman.direction == 'D') {
			pacman.image = pacmanDownImage;

		} else if (pacman.direction == 'L') {
			pacman.image = pacmanLeftImage;

		} else if (pacman.direction == 'R') {
			pacman.image = pacmanRightImage;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}