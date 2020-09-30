--[[
    GD50
    Breakout Remake

    -- PlayState Class --

    Author: Colton Ogden
    cogden@cs50.harvard.edu

    Represents the state of the game in which we are actively playing;
    player should control the paddle, with the ball actively bouncing between
    the bricks, walls, and the paddle. If the ball goes below the paddle, then
    the player should lose one point of health and be taken either to the Game
    Over screen if at 0 health or the Serve screen otherwise.
]]

PlayState = Class{__includes = BaseState}

--[[
    We initialize what's in our PlayState via a state table that we pass between
    states as we go from playing to serving.
]]
function PlayState:enter(params)
    self.paddle = params.paddle
    self.bricks = params.bricks
    self.health = params.health
    self.score = params.score
    self.highScores = params.highScores
    self.ball = params.ball
    self.level = params.level
    self.threshold = 0

    --Check to see if lock brick present

    self.buster = false

    self.balls = {}
    table.insert(self.balls, self.ball)

    self.recoverPoints = 5000

    -- give ball random starting velocity
    self.ball.dx = math.random(-200, 200)
    self.ball.dy = math.random(-50, -60)
    self.powerup = false
end

function PlayState:update(dt)
    if self.paused then
        if love.keyboard.wasPressed('space') then
            self.paused = false
            gSounds['pause']:play()
        else
            return
        end
    elseif love.keyboard.wasPressed('space') then
        self.paused = true
        gSounds['pause']:play()
        return
    end

    --If a powerup is present, update its position
    if not self.powerup == false then
        self.powerup:update(dt)
        --If a powerup reaches the end of the screen, stop keeping track of it
        if self.powerup.y >= VIRTUAL_HEIGHT then
            self.powerup = false
        end

    end

    -- update positions based on velocity
    self.paddle:update(dt)

    for j, ball in pairs(self.balls) do
        ball:update(dt)
    end

    --Check paddle collision with powerup
    if not self.powerup == false then
        if self.powerup:collides(self.paddle) then
            if self.powerup.skin == 4 then
                new_balls = {}
                for j, ball in pairs(self.balls) do
                    table.insert(new_balls, Ball(self.ball.skin, ball.x, ball.y, math.random(-200, 200), math.random(-50, -60)))
                    table.insert(new_balls, Ball(self.ball.skin, ball.x, ball.y, math.random(-200, 200), math.random(-50, -60)))
                end
                for j, ball in pairs(new_balls) do
                    table.insert(self.balls, ball)
                end
                
            elseif self.powerup.skin == 10 then
                self.buster = true
            end

            self.powerup = false
        end
    end

    for j, ball in pairs(self.balls) do 
        if ball:collides(self.paddle) then
            -- raise ball above paddle in case it goes below it, then reverse dy
            ball.y = self.paddle.y - 8
            ball.dy = -ball.dy

        --
        -- tweak angle of bounce based on where it hits the paddle
        --

        -- if we hit the paddle on its left side while moving left...
            if ball.x < self.paddle.x + (self.paddle.width / 2) and self.paddle.dx < 0 then
                ball.dx = -50 + -(8 * (self.paddle.x + self.paddle.width / 2 - ball.x))
        
        -- else if we hit the paddle on its right side while moving right...
            elseif ball.x > self.paddle.x + (self.paddle.width / 2) and self.paddle.dx > 0 then
                ball.dx = 50 + (8 * math.abs(self.paddle.x + self.paddle.width / 2 - ball.x))
            end

            gSounds['paddle-hit']:play()
        end
    end

    self.locks = false
    self.others = false

    for k,brick in pairs(self.bricks) do
        --If there's a lock on the board, set locks to true
        if brick.lock and brick.inPlay then
            self.locks = true
        elseif not brick.lock and brick.inPlay then
            self.others = true
        end
    end


    for j, ball in pairs(self.balls) do
    -- detect collision across all bricks with the ball
        for k, brick in pairs(self.bricks) do

        -- only check collision if we're in play
            if brick.inPlay and ball:collides(brick) then

                if not brick.lock or self.buster then
            -- add to score
                    self.score = self.score + (brick.tier * 200 + brick.color * 25)
                    self.threshold = self.threshold + (brick.tier * 200 + brick.color * 25)

            -- trigger the brick's hit function, which removes it from play
                    brick:hit()
                end

                --if there are only lock bricks, guarantees key powerup production
                if not self.others and not self.powerup and not self.buster then
                    self.powerup = Powerup('key', brick.x + 8, brick.y + 16)
                end
            --if no powerup already present, 1/5 chance of generating power up
                power = math.random(1,5)
                if power == 5 and not self.powerup then
                    if self.locks and not self.buster then
                        odds = math.random(1,2)
                    else
                        odds = 1
                    end

                    if odds == 1 then
                        self.powerup = Powerup('triple', brick.x + 8, brick.y + 16)
                    else
                        self.powerup = Powerup('key', brick.x + 8, brick.y + 16)
                    end
                end

            -- if we have enough points, recover a point of health
                if self.score > self.recoverPoints then
                -- can't go above 3 health
                    self.health = math.min(3, self.health + 1)

                -- multiply recover points by 2
                    self.recoverPoints = math.min(100000, self.recoverPoints * 2)

                -- play recover sound effect
                    gSounds['recover']:play()
                end

            -- go to our victory screen if there are no more bricks left
                if self:checkVictory() then
                    gSounds['victory']:play()

                    self.paddle.width = 64
                    self.paddle.size = 2

                    gStateMachine:change('victory', {
                        level = self.level,
                        paddle = self.paddle,
                        health = self.health,
                        score = self.score,
                        highScores = self.highScores,
                        ball = self.ball,
                        recoverPoints = self.recoverPoints
                    })
                end

            --
            -- collision code for bricks
            --
            -- we check to see if the opposite side of our velocity is outside of the brick;
            -- if it is, we trigger a collision on that side. else we're within the X + width of
            -- the brick and should check to see if the top or bottom edge is outside of the brick,
            -- colliding on the top or bottom accordingly 
            --

            -- left edge; only check if we're moving right, and offset the check by a couple of pixels
            -- so that flush corner hits register as Y flips, not X flips
                if ball.x + 2 < brick.x and ball.dx > 0 then
                
                -- flip x velocity and reset position outside of brick
                    ball.dx = -ball.dx
                    ball.x = brick.x - 8
            
            -- right edge; only check if we're moving left, , and offset the check by a couple of pixels
            -- so that flush corner hits register as Y flips, not X flips
                elseif ball.x + 6 > brick.x + brick.width and ball.dx < 0 then
                
                -- flip x velocity and reset position outside of brick
                    ball.dx = -ball.dx
                    ball.x = brick.x + 32
            
            -- top edge if no X collisions, always check
                elseif ball.y < brick.y then
                
                -- flip y velocity and reset position outside of brick
                    ball.dy = -ball.dy
                    ball.y = brick.y - 8
            
            -- bottom edge if no X collisions or top collision, last possibility
                else
                
                -- flip y velocity and reset position outside of brick
                    ball.dy = -ball.dy
                    ball.y = brick.y + 16
                end

            -- slightly scale the y velocity to speed up the game, capping at +- 150
                if math.abs(ball.dy) < 150 then
                    ball.dy = ball.dy * 1.02
                end

            -- only allow colliding with one brick, for corners
                break
            end
        end
    end

    for j,ball in pairs(self.balls) do
        if ball.y >= VIRTUAL_HEIGHT then
            gSounds['hurt']:play()
            table.remove(self.balls, j)
        end
    end

    if self.threshold > 10000 and self.paddle.size < 4 then
        self.paddle.size = self.paddle.size + 1
        self.paddle.width = self.paddle.width + 32
        self.threshold = 0
    end

    -- if ball goes below bounds, revert to serve state and decrease health
    if #self.balls == 0 then
        self.health = self.health - 1
        self.buster = false
        self.threshold = 0
        
        if self.health == 0 then
            gStateMachine:change('game-over', {
                score = self.score,
                highScores = self.highScores
            })
        else
            if self.paddle.size > 1 then
                self.paddle.size = self.paddle.size - 1
                self.paddle.width = self.paddle.width - 32
            end
            gStateMachine:change('serve', {
                paddle = self.paddle,
                bricks = self.bricks,
                health = self.health,
                score = self.score,
                highScores = self.highScores,
                level = self.level,
                recoverPoints = self.recoverPoints
            })
            
        end

    end

    -- for rendering particle systems
    for k, brick in pairs(self.bricks) do
        brick:update(dt)
    end

    if love.keyboard.wasPressed('escape') then
        love.event.quit()
    end
end

function PlayState:render()
    -- render bricks
    for k, brick in pairs(self.bricks) do
        brick:render()
    end

    -- render all particle systems
    for k, brick in pairs(self.bricks) do
        brick:renderParticles()
    end

    self.paddle:render()
    for j, ball in pairs(self.balls) do
        ball:render()
    end
    

    if not self.powerup == false then
        self.powerup:render()
    end

    renderScore(self.score)
    renderHealth(self.health)

    -- pause text, if paused
    if self.paused then
        love.graphics.setFont(gFonts['large'])
        love.graphics.printf("PAUSED", 0, VIRTUAL_HEIGHT / 2 - 16, VIRTUAL_WIDTH, 'center')
    end
end

function PlayState:checkVictory()
    for k, brick in pairs(self.bricks) do
        if brick.inPlay then
            return false
        end 
    end

    return true
end