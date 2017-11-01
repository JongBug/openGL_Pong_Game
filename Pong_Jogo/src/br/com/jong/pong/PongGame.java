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

	float x;
	float y;
	float tx = 0;
	float ty = 0;
	float limiteEsq = -6;
	float limiteDir = 6;
	float limiteBaixo = -6;
	float limiteTopo = 6;
	private float angulo;
	private float incAngulo;
	private float tamanho;
	private float incTamanho;
	Boolean wireOn = false;
	Boolean onLuz = false;
	int TONALIZACAO = GL2.GL_SMOOTH;
	float luzR = 0.0f, luzG = 0.0f, luzB = 0.0f;

	// Define constants for the top-level container
	private static String TITULO = "Colisão";
	private static final int CANVAS_LARGURA = 500; // largura do drawable
	private static final int CANVAS_ALTURA = 500; // altura do drawable
	private static final int FPS = 60; // define frames per second para a animacao

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Cria a janela de renderizacao OpenGL
				GLCanvas canvas = new PongGame();
				canvas.setPreferredSize(new Dimension(CANVAS_LARGURA, CANVAS_ALTURA));
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

//		iluminacao();
//		// criar a cena aqui....
		gl.glPushMatrix();
			gl.glPushMatrix();
				movimentoBola();
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
		// gl.glPushMatrix();
		// gl.glTranslatef(tx, 0, 0);
		// gl.glTranslatef(0, ty, 0);
		// quadrado();
		// gl.glPopMatrix();

		emitirMensagemColisao();

		// Executa os comandos OpenGL
		gl.glFlush();
	}
	float barraMax;
	float barraMin;
	public void espacoBarra () {
		barraMax = tx + 15;
		barraMin = tx - 15;
	}
	
	float xInicio;
	float yInicio;
	int caseBola = 1;
	public void movimentoBola () {
		if(y <= yMin+5) {
			System.out.println("Perdeu");
		} else
		if(x < xMax-5 && x > xMin+5 && y < yMax-5 && y > yMin +10) {
			switch (caseBola) {
				case 1:
					gl.glColor3f(0.81f,0.71f,0.23f); // OldGold 
					x+=1.6; y+=1.5;
					break;
					
				//Parede xMAX 
				case 2:
					gl.glColor3f(0.22f,0.69f,0.87f); // Summer Sky
					x-=1.7; y-=1.6;
					break;
				//Parede xMAX  
				case 3:
					gl.glColor3f(0.22f,0.69f,0.87f); // Summer Sky
					x-=1.6; y+=1.7;
					break;
					
				//Parede yMAX 
				case 4:
					gl.glColor3f(0.196078f,0.6f,0.8f); //  SkyBlue 
					x+=1.5; y-=1.5;
					break;
				//Parede yMAX  
				case 5:
					gl.glColor3f(0.196078f,0.6f,0.8f); //  SkyBlue 
					x-=1.7; y-=1.6;
					break;
				
				//Parede xMIN 
				case 6:
					gl.glColor3f(0.13f,0.37f,0.31f); // Verde Hunter
					x+=1.6; y+=1.7;
					break;
				//Parede xMIN  
				case 7:
					gl.glColor3f(0.13f,0.37f,0.31f); // Verde Hunter
					x+=1.6; y-=1.5;
					break;
					
				//Parede yMIN 
				case 8:
					gl.glColor3f(0.8f,0.196078f, 0.6f); // VioletRed 
					x+=1.6; y+=1.7;
					break;
				//Parede yMIN  
				case 9:
					gl.glColor3f(0.8f,0.196078f, 0.6f); // VioletRed 
					x-=1.7; y+=1.5;
					break;
			}

		} else {
			if (x <= xMin+5) {
				gl.glColor3f(0.13f,0.37f,0.31f); // Verde Hunter
				if(yInicio < y) {
					caseBola = 6; //x++; y++;
				} else if (yInicio > y){
					caseBola = 7; //x++; y--;
				}
				x++;
				yInicio = y;	
				xInicio = x;
				
			} else if (x >= xMax-5) {
				gl.glColor3f(0.22f,0.69f,0.87f); // Summer Sky
				if(yInicio < y) {
					caseBola = 3; //x--; y++;
				} else if (yInicio > y){
					caseBola = 2; //x--; y--;
				}
				x--;
				yInicio = y;	
				xInicio = x;
				
			} else if (y <= yMin+10) {
				if(barraMin <= x && x <= barraMax) {
					gl.glColor3f(0.8f,0.196078f, 0.6f); // VioletRed 
					float barraMedia = ((barraMax+barraMin)/2);
					if(barraMedia < x) {
						caseBola = 8; //x++; y++;
						x +=1.7;
						y +=1.8;
					} else if (barraMedia > x){
						caseBola = 9; //x--; y++;
						x -=1.7;
						y +=1.8;
					}
					y++;
					xInicio = x;	
					yInicio = y;					
				} else {
					y--;
					x--;
				}
						
			} else if (y >= yMax-5) {
				gl.glColor3f(0.196078f,0.6f,0.8f); //  SkyBlue 
				if(xInicio < x) {
					caseBola = 4; //x++; y--;
				} else if (xInicio > x){
					caseBola = 5; //x--; y--;
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
	
    private void rotacionaBola() {
        angulo = angulo + incAngulo;
        if (angulo > 360f) {
            angulo = angulo - 360;
        }
//       System.out.println("ANGULO: " + (int)angulo);
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

	// public void updateUp(){
	// if(ty < limiteTopo)
	// ty += 1;
	// }
	//
	// public void updateDown(){
	// if(ty > limiteBaixo)
	// ty --;
	// }

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
		}
			char keyChar = e.getKeyChar();
			switch (keyChar) {
			case KeyEvent.VK_LEFT:
				updateLeft();
				break;

			case KeyEvent.VK_RIGHT:
				updateRight();
				break;
				
			case 'd':
				updateRight();
				break;
				
			case 'a':
				updateLeft();
				break;
				
			case 'w':
				System.out.println("o");
				if (wireOn) {
					wireOn = false;
				} else {
					wireOn = true;
				}
				break;
				
			case 'q':
				//inicia animacao
				incAngulo = 35.0f;
				break;
			// case KeyEvent.VK_UP:
			// updateUp();
			// break;
			//
			// case KeyEvent.VK_DOWN:
			// updateDown();
			// break;
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
		// float luzAmbiente[] = {0.2f, 0.2f, 0.2f, 1.0f};
		// float luzAmbiente[] = {1.0f, 0.5f, 0.0f, 1.0f};
		float luzAmbiente[] = new float[4];
		luzAmbiente[0] = luzR;
		luzAmbiente[1] = luzG;
		luzAmbiente[2] = luzB;
		luzAmbiente[3] = 1.0f;

		float luzDifusa[] = { 0.7f, 0.7f, 0.7f, 1.0f };
		float luzEspecular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		float posicaoLuz[] = { 0.0f, 50.0f, 50.0f, 0.0f };

		// capacidade de brilho do material
		float especularidade[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		int especMaterial = 60;

		// define a concentração do brilho
		gl.glMateriali(GL2.GL_FRONT, GL2.GL_SHININESS, especMaterial);

		// ativa o uso da luz ambiente
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, luzAmbiente, 0);

		// define os parâmetros de luz de número 0 (zero)
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, luzAmbiente, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, luzDifusa, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, luzEspecular, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, posicaoLuz, 0);
	}
}
