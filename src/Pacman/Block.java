package Pacman;

import java.awt.Image;

public class Block {
	public int x;
	public int y;
	public int width;
	public int height;
	public Image image;

	public int startX;
	public int startY;
	public char direction = 'U';
	public int velocityX = 0;
	public int velocityY = 0;

	public Block(Image image, int x, int y, int width, int height) {
		this.image = image;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.startX = x;
		this.startY = y;
	}

	public void updateDirection(char direction, int tileSize, java.util.HashSet<Block> walls, PacMan game) {
		char prevDirection = this.direction;
		this.direction = direction;
		updateVelocity(tileSize);
		this.x += this.velocityX;
		this.y += this.velocityY;

		for (Block wall : walls) {
			if (game.collision(this, wall)) {
				this.x -= this.velocityX;
				this.y -= this.velocityY;
				this.direction = prevDirection;
				updateVelocity(tileSize);
			}
		}
	}

	public void updateVelocity(int tileSize) {
		if (this.direction == 'U') {
			this.velocityX = 0;
			this.velocityY = -tileSize / 4;
		} else if (this.direction == 'D') {
			this.velocityX = 0;
			this.velocityY = tileSize / 4;
		} else if (this.direction == 'L') {
			this.velocityX = -tileSize / 4;
			this.velocityY = 0;
		} else if (this.direction == 'R') {
			this.velocityX = tileSize / 4;
			this.velocityY = 0;
		}
	}

	public void reset() {
		this.x = this.startX;
		this.y = this.startY;
	}
}