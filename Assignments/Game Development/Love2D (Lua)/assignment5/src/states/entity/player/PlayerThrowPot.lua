PlayerThrowPot = Class{__includes = BaseState}

function PlayerThrowPot:init(player, dungeon)
    self.player = player
    self.dungeon = dungeon

    -- render offset for spaced character sprite
    self.player.offsetY = 5
    self.player.offsetX = 0

    local direction = self.player.direction
    self.player.lifting = true
    self.player.lift = false
    self.player:changeAnimation('throw-' .. self.player.direction)
    self.hitbox = true

    if direction == 'left' then
        Timer.tween(0.4, {
            [self.player.pot] = {x = self.player.x - 64}
        })
        Timer.tween(0.4, {
            [self.player.pot] = {y = self.player.y + 7}
        })
        :finish(function() 
            self.player.lifting = false
            for k = 1, #self.dungeon.currentRoom.objects do
                if self.dungeon.currentRoom.objects[k] == self.player.pot then
                    table.remove(self.dungeon.currentRoom.objects, k)
                    self.player.pot = false
                    break
                end
            end
            if self.hitbox then
                gSounds['hit-enemy']:play()
            end
        end)  
    elseif direction == 'right' then
        Timer.tween(0.4, {
            [self.player.pot] = {x = self.player.x + 64}
        })
        Timer.tween(0.4, {
            [self.player.pot] = {y = self.player.y + 7}
        })
        :finish(function() 
            self.player.lifting = false
            for k = 1, #self.dungeon.currentRoom.objects do
                if self.dungeon.currentRoom.objects[k] == self.player.pot then
                    table.remove(self.dungeon.currentRoom.objects, k)
                    self.player.pot = false
                    break
                end
            end
            if self.hitbox then
                gSounds['hit-enemy']:play()
            end
        end)  
    elseif direction == 'up' then
        Timer.tween(0.4, {
            [self.player.pot] = {y = self.player.y - 64}
        })
        :finish(function() 
            self.player.lifting = false
            for k = 1, #self.dungeon.currentRoom.objects do
                if self.dungeon.currentRoom.objects[k] == self.player.pot then
                    table.remove(self.dungeon.currentRoom.objects, k)
                    self.player.pot = false
                    break
                end
            end
            if self.hitbox then
                gSounds['hit-enemy']:play()
            end
        end)    
    else
        Timer.tween(0.4, {
            [self.player.pot] = {y = self.player.y + 64}
        })
        :finish(function() 
            self.player.lifting = false
            for k = 1, #self.dungeon.currentRoom.objects do
                if self.dungeon.currentRoom.objects[k] == self.player.pot then
                    table.remove(self.dungeon.currentRoom.objects, k)
                    self.player.pot = false
                    break
                end
            end
            if self.hitbox then
                gSounds['hit-enemy']:play()
            end
        end)    
    end

end

function PlayerThrowPot:enter(params)
    
end

function PlayerThrowPot:update(dt)
    if self.player.currentAnimation.timesPlayed > 0 then
        self.player.currentAnimation.timesPlayed = 0
        self.player:changeState('idle')
    end

    if self.hitbox then
        if self.player.pot.x < MAP_RENDER_OFFSET_X or self.player.pot.y < MAP_RENDER_OFFSET_Y or self.player.pot.x > VIRTUAL_WIDTH - 2 * MAP_RENDER_OFFSET_X or self.player.pot.y > VIRTUAL_HEIGHT - 2 * MAP_RENDER_OFFSET_Y then
            self.hitbox = false
            self.player.lift = false
            self.player.lifting = false
            for k = 1, #self.dungeon.currentRoom.objects do
                if self.dungeon.currentRoom.objects[k] == self.player.pot then
                    table.remove(self.dungeon.currentRoom.objects, k)
                    self.player.pot = false
                    break
                end
            end
            gSounds['hit-enemy']:play()
        end
        
    end

    if self.hitbox then
        self.potHitBox = Hitbox(self.player.pot.x, self.player.pot.y, 16, 16)
        for k, entity in pairs(self.dungeon.currentRoom.entities) do
            if not entity.dead and entity:collides(self.potHitBox) then
                self.player.lift = false
                self.player.lifting = false
                for k = 1, #self.dungeon.currentRoom.objects do
                    if self.dungeon.currentRoom.objects[k] == self.player.pot then
                        table.remove(self.dungeon.currentRoom.objects, k)
                        self.player.pot = false
                        break
                    end
                end
                entity:damage(1)
                gSounds['hit-enemy']:play()
                self.hitbox = false
                break
            end
        end
    end


    
end

function PlayerThrowPot:render()
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