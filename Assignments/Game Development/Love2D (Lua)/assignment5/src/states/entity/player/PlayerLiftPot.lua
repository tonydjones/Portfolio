PlayerLiftPot = Class{__includes = BaseState}

function PlayerLiftPot:init(player, dungeon)
    self.player = player
    self.dungeon = dungeon

    -- render offset for spaced character sprite
    self.player.offsetY = 5
    self.player.offsetX = 0
    self.player.lifting = true

    local direction = self.player.direction

    self.player:changeAnimation('lift-' .. self.player.direction)

    Timer.tween(0.25, {
        [self.player.pot] = {x = self.player.x - 1}
    })
    Timer.tween(0.25, {
        [self.player.pot] = {y = self.player.y - 7}
    })

    :finish(function() 
        self.player.lift = true
        self.player.lifting = false 
    end)

end

function PlayerLiftPot:enter(params)
    
end

function PlayerLiftPot:update(dt)

    if self.player.currentAnimation.timesPlayed > 0 then
        self.player.currentAnimation.timesPlayed = 0
        self.player:changeState('idle')
    end


end

function PlayerLiftPot:render()
    local anim = self.player.currentAnimation
    love.graphics.draw(gTextures[anim.texture], gFrames[anim.texture][anim:getCurrentFrame()],
        math.floor(self.player.x - self.player.offsetX), math.floor(self.player.y - self.player.offsetY))

    -- debug for player and hurtbox collision rects
    -- love.graphics.setColor(255, 0, 255, 255)
    -- love.graphics.rectangle('line', self.player.x, self.player.y, self.player.width, self.player.height)
    -- love.graphics.rectangle('line', self.swordHurtbox.x, self.swordHurtbox.y,
    --     self.swordHurtbox.width, self.swordHurtbox.height)
    -- love.graphics.setColor(255, 255, 255, 255)
end