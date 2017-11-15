package br.com.jong.pong;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.FPSAnimator;
import java.math.*;

@SuppressWarnings("serial")
public class PongGame extends GLCanvas implements GLEventListener, KeyListener {

	private GL2 gl;
	private GLU glu;
	private GLUT glut;

	// Para definir as Coordenadas do sistema
	float xMin, xMax, yMin, yMax, zMin, zMax;

	int vida = 5;
	boolean hard = false;
	int pontuacao = 0;
	boolean iniciar = true;
	boolean start = true;
	boolean pauseMsg = false;
	float x;
	float y;
	float tx = 0;
	float ty = 0;
	float limiteEsq = -6;
	float limiteDir = 6;
	float limiteBaixo = -6;
	float limiteTopo = 6;
	private float angulo;
	private float incAngulo = 35.0f;
	private float incText = 4.0f;
	float anguloText;
	float anguloTextLento;
	private float incTextLento = 0.5f;
	Boolean wireOn = false;

	int TONALIZACAO = GL2.GL_SMOOTH;
	float luzR = 0.0f, luzG = 0.0f, luzB = 0.0f;

	// Define constants for the top-level container
	private static String TITULO = "Pong";
	// private static final int CANVAS_LARGURA = 1000; // largura do drawable
	// private static final int CANVAS_ALTURA = 800; // altura do drawable
	private static final int FPS = 60; // define frames per second para a animacao

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				System.out.println("Nome: Jong Hwa Lee, Bruna Nobrega e Caique Cassemiro\nRA: 20562792, ...., .....");
				// Cria a janela de renderizacao OpenGL
				GLCanvas canvas = new PongGame();
				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

				canvas.setPreferredSize(new Dimension(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight()));
				final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);
				final JFrame frame = new JFrame();

				frame.getContentPane().add(canvas);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						new Thread() {
							@Override
							public void run() {
								if (animator.isStarted())
									animator.stop();
								System.exit(0);
							}
						}.start();
					}
				});
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				frame.setUndecorated(true);
				frame.setVisible(true);
				frame.setTitle(TITULO);
				frame.pack();
				frame.setLocationRelativeTo(null); // Center frame
				frame.setVisible(true);
				animator.start(); // inicia o loop de animacao
			}
		});
	}

	/** Construtor da classe */
	public PongGame() {
		this.addGLEventListener(this);

		this.addKeyListener(this);
		this.setFocusable(true);
		this.requestFocus();
	}

	/**
	 * Chamado uma vez quando o contexto OpenGL eh criado
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2(); // obtem o contexto grafico OpenGL
		glu = new GLU();

		// Estabelece as coordenadas do SRU (Sistema de Referencia do Universo)
		xMin = -100;
		xMax = 100;
		yMin = -100;
		yMax = 100;
		zMin = -100;
		zMax = 100;

		angulo = 0;
		incAngulo = 0;

		// Habilita o buffer de profundidade
		gl.glEnable(GL2.GL_DEPTH_TEST);
	}

	/**
	 * Chamado quando a janela eh redimensionada
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		gl = drawable.getGL().getGL2(); // obtem o contexto grafico OpenGL

		// Ativa a matriz de projecao
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		// Projecao ortogonal 3D
		gl.glOrtho(xMin, xMax, yMin, yMax, zMin, zMax);

		// Ativa a matriz de modelagem
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		System.out.println("Reshape: " + width + " " + height);
	}

	/**
	 * Chamado para renderizar a imagem do GLCanvas pelo animator
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2(); // obtem o contexto grafico OpenGL
		glut = new GLUT();

		// Especifica que a cor para limpar a janela de visualizacao eh preta
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Limpa a janela de visualizacao com a cor de fundo especificada
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		// Redefine a matriz atual com a matriz "identidade"
		gl.glLoadIdentity();
		ligaLuz();
		iluminacao();
		// // criar a cena aqui....
		if (start) {
			gl.glPushMatrix();
			gl.glTranslatef(-75, 0, 0);
			apresentacao();
			gl.glPopMatrix();
		} else if (pauseMsg) {
			gl.glPushMatrix();
			gl.glTranslatef(-75, 0, 0);
			pause();
			gl.glPopMatrix();
		} else {
			gl.glPushMatrix();
			gl.glTranslatef(55, 85, 0);
			msgVida();
			gl.glPopMatrix();
			gl.glPushMatrix();
			gl.glTranslatef(65, 88, 0);
			if (vida >= 5) {
				vidaEsfera(3);
			} else {
				vidaEsfera(0);
			}
			gl.glPopMatrix();
			gl.glPushMatrix();
			gl.glTranslatef(72, 88, 0);
			if (vida >= 4) {
				vidaEsfera(3);
			} else {
				vidaEsfera(0);
			}
			gl.glPopMatrix();
			gl.glPushMatrix();
			gl.glTranslatef(79, 88, 0);
			if (vida >= 3) {

				vidaEsfera(3);
			} else {
				vidaEsfera(0);
			}
			gl.glPopMatrix();
			gl.glPushMatrix();
			gl.glTranslatef(86, 88, 0);
			if (vida >= 2) {
				vidaEsfera(3);
			} else {
				vidaEsfera(0);
			}
			gl.glPopMatrix();
			gl.glPushMatrix();
			gl.glTranslatef(93, 88, 0);
			if (vida >= 1) {
				vidaEsfera(3);
			} else {
				vidaEsfera(0);
			}
			gl.glPopMatrix();
			if (iniciar) {
				gl.glPushMatrix();
				gl.glPushMatrix();
				if (nivelPontuacao()) {
					hard = true;
					gl.glPushMatrix();
					barraDificil();
					gl.glPopMatrix();
					movimentoBola(0.4f);
				} else {
					movimentoBola(0);
				}

				gl.glTranslatef(x, y, 0);
				gl.glRotatef(angulo, 1, 0, 1);
				rotacionaBola();
				turnOnWire(wireOn);

				bolaJogo();
				gl.glPopMatrix();

				gl.glPushMatrix();
				gl.glTranslatef(tx, 0, 0);
				barra();
				espacoBarra();
				gl.glPopMatrix();

				gl.glPopMatrix();
			} else {
				gl.glPushMatrix();
				gl.glTranslatef(-85, 0, 0);
				msgPerdeu();
				gl.glPopMatrix();
			}
		}
		// Executa os comandos OpenGL
		gl.glFlush();
	}

	public void pause() {
		gl.glRotatef(anguloText, 1, 0, 1);
		rotacionatexto();
		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(5f, 9.0f);
		glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "PAUSE");
	}
	public void rotacionatexto() {
		anguloText = anguloText + incText;
		if (anguloText > 360f) {
			anguloText = anguloText - 360;
		}
		// System.out.println("ANGULO: " + (int)angulo);
	}
	public void barraDificil() {
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2f(15, 10);
		gl.glVertex2f(15, 0);
		gl.glVertex2f(-15, 0);
		gl.glVertex2f(-15, 10);
		gl.glEnd();
	}

	public boolean nivelPontuacao() {
		if (pontuacao >= 200) {
			return true;
		} else {
			return false;
		}
	}

	public void apresentacao() {
		gl.glRotatef(anguloTextLento, 1, 0, 1);
		rotacionatextoLento();
		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(5f, 9.0f);
		glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "Bem vindo ao jogo Pong, o deslocamento do");
		gl.glTranslatef(0, -10, 0);
		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(5f, 9.0f);
		glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "bastão pode ser feito pelo teclado");
		gl.glTranslatef(0, -10, 0);
		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(5f, 9.0f);
		glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "aperte 'c' para começar o jogo");
		gl.glTranslatef(0, -10, 0);
		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(5f, 9.0f);
		glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "aperte 's' para stop o jogo");
		gl.glTranslatef(0, -10, 0);
		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(5f, 9.0f);
		glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "aperte 'p' para pausar o jogo");
	}
	
	public void rotacionatextoLento() {
		anguloTextLento = anguloTextLento + incTextLento;
		if (anguloTextLento > 360f) {
			anguloTextLento = anguloTextLento - 360;
		}
		// System.out.println("ANGULO: " + (int)angulo);
	}

	float barraMax;
	float barraMin;

	public void espacoBarra() {
		barraMax = tx + 15;
		barraMin = tx - 15;
	}

	public void inicializar(boolean iniciar) {
		if (iniciar)
			iniciar = false;
		else {
			iniciar = true;
		}
	}

	public void msgVida() {
		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(5f, 9.0f);
		glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "Pontuacao:" + pontuacao);
	}

	public void vidaEsfera(int vida) {
		gl.glRotatef(angulo, 1, 0, 1);
		rotacionaBola();
		gl.glColor3f(0.22f, 0.69f, 0.87f); // Summer Sky
		glut.glutSolidSphere(vida, 15, 15);
	}

	public void msgPerdeu() {
		if (vida <= 0) {
			gl.glColor3f(1, 1, 1);
			gl.glRasterPos2f(5f, 9.0f);
			glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "Perdeu o jogo, aperte 'r' para recomeçar um novo jogo");
		} else {
			gl.glColor3f(1, 1, 1);
			gl.glRasterPos2f(5f, 9.0f);
			glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "Perdeu uma vida, aperte 'i' para iniciar novamente.");
		}
	}

	float xInicio;
	float yInicio;
	int caseBola = 1;

	public void movimentoBola(float nivel) {
		if (y <= yMin + 5) {
			if (vida <= 0) {
				pontuacao = 0;
				iniciar = false;
			} else {
				iniciar = false;
				pontuacao = 0;
				vida--;
				x = -68f;
				y = 38f;
				caseBola = 1;
			}
		} else if (x < xMax - 5 && x > xMin + 5 && y < yMax - 5 && y > yMin + 10) {
			if (hard) {
				if (x <= 15 && x >= -15 && y <= 10 && y >= -10) {
					if (y >= -10 && y <= -8) {
						caseBola = 4;
					} else if (x >= -15 && x <= -13) {
						caseBola = 3;
					} else if (y <= 10 && y >= 8) {
						caseBola = 8;
					} else if (x <= 15 && x >= 13) {
						caseBola = 4;
					}
				}
			}
			switch (caseBola) {
			case 1:
				gl.glColor3f(0.81f, 0.71f, 0.23f); // OldGold
				x += 1.6 + nivel;
				y += 1.5 + nivel;
				break;

			// Parede xMAX
			case 2:
				gl.glColor3f(0.22f, 0.69f, 0.87f); // Summer Sky
				x -= 1.7 + nivel;
				y -= 1.6 + nivel;
				break;
			// Parede xMAX
			case 3:
				gl.glColor3f(0.22f, 0.69f, 0.87f); // Summer Sky
				x -= 1.6 + nivel;
				y += 1.7 + nivel;
				break;

			// Parede yMAX
			case 4:
				gl.glColor3f(0.196078f, 0.6f, 0.8f); // SkyBlue
				x += 1.5 + nivel;
				y -= 1.5 + nivel;
				break;
			// Parede yMAX
			case 5:
				gl.glColor3f(0.196078f, 0.6f, 0.8f); // SkyBlue
				x -= 1.7 + nivel;
				y -= 1.6 + nivel;
				break;

			// Parede xMIN
			case 6:
				gl.glColor3f(0.13f, 0.37f, 0.31f); // Verde Hunter
				x += 1.6 + nivel;
				y += 1.7 + nivel;
				break;
			// Parede xMIN
			case 7:
				gl.glColor3f(0.13f, 0.37f, 0.31f); // Verde Hunter
				x += 1.6 + nivel;
				y -= 1.5 + nivel;
				break;

			// Parede yMIN
			case 8:
				gl.glColor3f(0.8f, 0.196078f, 0.6f); // VioletRed
				x += 1.6 + nivel;
				y += 1.7 + nivel;
				break;
			// Parede yMIN
			case 9:
				gl.glColor3f(0.8f, 0.196078f, 0.6f); // VioletRed
				x -= 1.7 + nivel;
				y += 1.5 + nivel;
				break;
			}

		} else {
			if (x <= xMin + 5) {
				gl.glColor3f(0.13f, 0.37f, 0.31f); // Verde Hunter
				if (yInicio < y) {
					caseBola = 6; // x++; y++;
				} else if (yInicio > y) {
					caseBola = 7; // x++; y--;
				}
				x++;
				yInicio = y;
				xInicio = x;

			} else if (x >= xMax - 5) {
				gl.glColor3f(0.22f, 0.69f, 0.87f); // Summer Sky
				if (yInicio < y) {
					caseBola = 3; // x--; y++;
				} else if (yInicio > y) {
					caseBola = 2; // x--; y--;
				}
				x--;
				yInicio = y;
				xInicio = x;

			} else if (y <= yMin + 10) {
				if (barraMin <= x && x <= barraMax) {
					pontuacao++;
					gl.glColor3f(0.8f, 0.196078f, 0.6f); // VioletRed
					float barraMedia = ((barraMax + barraMin) / 2);
					if (barraMedia < x) {
						caseBola = 8; // x++; y++;
						x += 1.7 + nivel;
						y += 1.8 + nivel;
					} else if (barraMedia > x) {
						caseBola = 9; // x--; y++;
						x -= 1.7 + nivel;
						y += 1.8 + nivel;
					}
					y++;
					xInicio = x;
					yInicio = y;
				} else {
					y--;
					if (xInicio < x) {
						x++;
					} else {
						x--;
					}

				}

			} else if (y >= yMax - 5) {
				gl.glColor3f(0.196078f, 0.6f, 0.8f); // SkyBlue
				if (xInicio < x) {
					caseBola = 4; // x++; y--;
				} else if (xInicio > x) {
					caseBola = 5; // x--; y--;
				}
				y--;
				xInicio = x;
				yInicio = y;
			}

		}
	}

	public void mensagem(String frase) {
		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(5f, 9.0f);
		glut.glutBitmapString(GLUT.BITMAP_8_BY_13, frase);
	}

	public void rotacionaBola() {
		angulo = angulo + incAngulo;
		if (angulo > 360f) {
			angulo = angulo - 360;
		}
		// System.out.println("ANGULO: " + (int)angulo);
	}

	public void bolaJogo() {
		glut.glutSolidSphere(5, 15, 15);
	}

	public void turnOnWire(Boolean wireOn) {

		if (wireOn) {
			gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_LINE);
		} else {
			gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
		}

	}

	public void barra() {
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2f(15, yMin + 5);
		gl.glVertex2f(15, yMin);
		gl.glVertex2f(-15, yMin);
		gl.glVertex2f(-15, yMin + 5);
		gl.glEnd();
	}

	public void emitirMensagemColisao() {
		if (tx == limiteDir || tx == limiteEsq || ty == limiteBaixo || ty == limiteTopo) {
			mensagem("Colidiu!!");
		}
	}

	public void updateLeft() {

		if (tx > xMin + 15)
			tx -= 5;
	}

	public void updateRight() {
		if (tx < xMax - 15)
			tx += 5;
	}

	public void stop() {
		start = true;
		pauseMsg = false;
		vida = 5;
		pontuacao = 0;
		hard = false;
		caseBola = 1;
		x = 0;
		y = 0;
	}

	/**
	 * Chamado quando o contexto OpenGL eh destruido
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch (keyCode) {
		case KeyEvent.VK_ESCAPE:
			System.exit(0);
			break;
		case KeyEvent.VK_LEFT:
			updateLeft();
			break;

		case KeyEvent.VK_RIGHT:
			updateRight();
			break;
		}
		char keyChar = e.getKeyChar();
		switch (keyChar) {
		case 'w':
			System.out.println("o");
			if (wireOn) {
				wireOn = false;
			} else {
				wireOn = true;
			}
			break;
		//
		// case 'q':
		// // inicia animacao
		// incAngulo = 35.0f;
		// break;

		case 'i':
			if (vida != 0) {
				if (iniciar) {
					iniciar = false;
				} else {
					iniciar = true;
				}
			}
			break;

		case 'r':
			if (vida == 0) {
				vida = 5;
				pontuacao = 0;
				iniciar = true;
				x = -68f;
				y = 38f;
			}
			break;

		case 'c':
			start = false;
			incAngulo = 35.0f;
			break;

		case 's':
			stop();
			break;

		case 'p':
			if (!start) {
				if (pauseMsg) {
					pauseMsg = false;
				} else {
					pauseMsg = true;
				}
			}
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void iluminacao() {
		// float luzAmbiente[] = {1.0f, 0.5f, 0.0f, 1.0f};
		float luzAmbiente[] = new float[4];
		luzAmbiente[0] = 0.2f;
		luzAmbiente[1] = 0.2f;
		luzAmbiente[2] = 0.2f;
		luzAmbiente[3] = 1.0f;

		float posicaoLuz[] = { 1.0f, 1.0f, 1.0f, 0.0f };

		// ativa o uso da luz ambiente
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, luzAmbiente, 0);

		// define os parâmetros de luz de número 0 (zero)
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, luzAmbiente, 0);

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, posicaoLuz, 0);
	}

	public void ligaLuz() {
		// habilita a definição da cor do material a partir da cor corrente
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		// habilita o uso da iluminação na cena
		gl.glEnable(GL2.GL_LIGHTING);
		// habilita a luz de número 0
		gl.glEnable(GL2.GL_LIGHT0);

		gl.glShadeModel(TONALIZACAO);
	}

}
