package com.matheus.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter
{
    private SpriteBatch batch;
    private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;
    private Circle circuloPassaro;
    private Rectangle retanguloCanoAlto;
    private Rectangle retanguloCanoBaixo;

    //private ShapeRenderer shape;

    private int movimento = 0;

    //ATRIBUTOS DE CONFIGURAÇÃO
    private float larguraDispositivo;
    private float alturaDispositivo;

    private int estadoJogo = 0; //0 -> NÃO INICIADO - 1->INICIADO - 2 -> GAME OVER
    private int pontuacao = 0;

    private float variacao = 0;
    private float velocidadeQueda = 0;
    private float posicaoInicialVertical;
    private float posicaoMovimentoCanoHorizontal;
    private float espacoEntreCanos;
    private float deltaTime;
    private float alturaEntreCanosRandomica;
    private BitmapFont fonte;
    private BitmapFont mensagem;
    private boolean marcouPonto = false;
    private Random numeroRandomico;


    //CÂMERA
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 768;
    private final float VIRTUAL_HEIGHT = 1024;

    //SONS
    private Sound somPulo;
    private Sound somPonto;
    private Sound somGameOver;

    @Override
    public void create()
    {
        batch = new SpriteBatch();
        numeroRandomico = new Random();

        circuloPassaro = new Circle();
        /*
        retanguloCanoBaixo = new Rectangle();
        retanguloCanoAlto = new Rectangle();
        shape = new ShapeRenderer();
        */


        //CONFIGURAÇÕES DO TEXTO DA PONTUAÇÃO
        fonte = new BitmapFont();
        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(6);

        mensagem = new BitmapFont();
        mensagem.setColor(Color.WHITE);
        mensagem.getData().setScale(3);

        somPulo = Gdx.audio.newSound(Gdx.files.internal("pulo.mp3"));
        somPonto = Gdx.audio.newSound(Gdx.files.internal("pontos.mp3"));
        somGameOver = Gdx.audio.newSound(Gdx.files.internal("gameover.mp3"));


        passaros = new Texture[4];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");
        passaros[3] = new Texture("passaroMorto.png");

        fundo = new Texture("fundo.png");
        canoBaixo = new Texture("cano_baixo.png");
        canoTopo = new Texture("cano_topo.png");
        gameOver = new Texture("game_over.png");

        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);

        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        larguraDispositivo = VIRTUAL_WIDTH; //Gdx.graphics.getWidth();
        alturaDispositivo = VIRTUAL_HEIGHT; //Gdx.graphics.getHeight();

        posicaoInicialVertical = alturaDispositivo / 2;
        posicaoMovimentoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 200;
    }

    @Override
    public void render()
    {
        camera.update();

        //LIMPAR FRAMES ANTERIORES PARA MELHORAR DESEMPENH DE MEMÓRIA
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        deltaTime = Gdx.graphics.getDeltaTime();

        //VELOCIDADE DAS ASAS BATENDO
        variacao = variacao + deltaTime * 10;
        if (variacao >= 3)
        {
            //variacao = variacao % passaros.length;
            variacao = 0;
        }

        //JOGO NÃO INICIADO
        if (estadoJogo == 0)
        {
            if (Gdx.input.justTouched())
            {
                estadoJogo = 1;
            }
        } else
        {
            velocidadeQueda++;
            //if (posicaoInicialVertical > 0 || Gdx.input.justTouched())
            if (posicaoInicialVertical > 0)
            {
                posicaoInicialVertical = posicaoInicialVertical - velocidadeQueda;
            }

            if (estadoJogo == 1)
            {
                //DEFINE A VELOCIDADE DE PASSAGEM DOS CANOS
                posicaoMovimentoCanoHorizontal = posicaoMovimentoCanoHorizontal - deltaTime * 200;

                //VERIFICA SE A TELA FOI TOCADA
                if (Gdx.input.justTouched())
                {
                    //LOG GDX
                    Gdx.app.log("Toque na tela", "tocou");
                    //DEFINE QUANTO O PASSÁRO SUBIRÁ A CADA TOQUE NA TELA
                    velocidadeQueda = -15;
                    somPulo.play();
                }

                //VERIFICA SE O CANO SAIU TOTALMENTE DA TELA
                if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth())
                {
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                    alturaEntreCanosRandomica = numeroRandomico.nextInt(400) - 200;
                    marcouPonto = false;
                }

                //VERIFICA PONTUAÇÃO
                if (posicaoMovimentoCanoHorizontal < 120)
                {
                    if (!marcouPonto)
                    {
                        marcouPonto = true;
                        pontuacao++;
                        somPonto.play();
                    }
                }
            }
            //GAME OVER
            else
            {
                if (Gdx.input.justTouched())
                {
                    estadoJogo = 0;
                    pontuacao = 0;
                    velocidadeQueda = 0;
                    marcouPonto = false;
                    posicaoInicialVertical = alturaDispositivo / 2;
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                }
            }
        }

        //CONFIGURAR DADOS DE PROJEÇÃO DA CÂMERA
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
        batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, (alturaDispositivo / 2) + (espacoEntreCanos / 2) + alturaEntreCanosRandomica);
        batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, ((alturaDispositivo / 2) - (canoBaixo.getHeight())) - (espacoEntreCanos / 2) + alturaEntreCanosRandomica);

        if (posicaoInicialVertical > 0 && estadoJogo != 2)
        {
            batch.draw(passaros[(int) variacao], 120, posicaoInicialVertical);
        }
        fonte.draw(batch, "" + pontuacao, larguraDispositivo / 2, alturaDispositivo - 50);

        if (estadoJogo == 2)
        {
            batch.draw(gameOver, (larguraDispositivo / 2) - (gameOver.getWidth() / 2), alturaDispositivo / 2);
            batch.draw(passaros[3], 75, posicaoInicialVertical);
            mensagem.draw(batch, "Toque para reiniciar", (larguraDispositivo / 2) - 200, (alturaDispositivo / 2) - (gameOver.getHeight() / 2));
        }

        batch.end();

        circuloPassaro.set((120 + (passaros[0].getWidth() / 2)), (posicaoInicialVertical + (passaros[0].getHeight() / 2)), (passaros[0].getWidth() / 2));

        retanguloCanoBaixo = new Rectangle(posicaoMovimentoCanoHorizontal, ((alturaDispositivo / 2) - (canoBaixo.getHeight())) - (espacoEntreCanos / 2) + alturaEntreCanosRandomica,
                canoBaixo.getWidth(), canoBaixo.getHeight());

        retanguloCanoAlto = new Rectangle(posicaoMovimentoCanoHorizontal, (alturaDispositivo / 2) + (espacoEntreCanos / 2) + alturaEntreCanosRandomica,
                canoTopo.getWidth(), canoTopo.getHeight());

        //DESENHAR FORMAR
        /*
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.circle(circuloPassaro.x, circuloPassaro.y, circuloPassaro.radius);
        shape.rect(retanguloCanoBaixo.x, retanguloCanoBaixo.y, retanguloCanoBaixo.width, retanguloCanoBaixo.height);
        shape.rect(retanguloCanoAlto.x, retanguloCanoAlto.y, retanguloCanoAlto.width, retanguloCanoAlto.height);
        shape.setColor(Color.RED);
        shape.end();
        */

        //TESTE DE COLISÃO
        if (Intersector.overlaps(circuloPassaro, retanguloCanoBaixo) || Intersector.overlaps(circuloPassaro, retanguloCanoAlto) || posicaoInicialVertical <= 0)
        //|| posicaoInicialVertical >= alturaDispositivo)
        //PARA TESTAR APENAS
        {
            //Gdx.app.log("COLISÃO", "Houve Colisão");

            //MUDA PARA GAME OVER O ESTADO
            estadoJogo = 2;
        }
    }

    @Override
    public void resize(int width, int height)
    {
        viewport.update(width, height);
    }
}
