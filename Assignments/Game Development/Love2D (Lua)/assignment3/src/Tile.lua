--[[
    GD50
    Match-3 Remake

    -- Tile Class --

    Author: Colton Ogden
    cogden@cs50.harvard.edu

    The individual tiles that make up our game board. Each Tile can have a
    color and a variety, with the varietes adding extra points to the matches.
]]

Tile = Class{}

function Tile:init(x, y, color, variety, multiplier)
    
    -- board positions
    self.gridX = x
    self.gridY = y

    -- coordinate positions
    self.x = (self.gridX - 1) * 32
    self.y = (self.gridY - 1) * 32

    -- tile appearance/points
    self.color = color
    self.variety = variety

    self.multiplier = multiplier

    --1 in 20 chance of the tile being shiny
    if math.random(20) == 20 then
        self.shiny = true
    else
        self.shiny = false
    end

    if self.shiny then
        self.psystem = love.graphics.newParticleSystem(gTextures['particle'], 5)
        self.psystem:setParticleLifetime(0.1)
        self.psystem:setLinearAcceleration(-15, -15, 15, 15)
        self.psystem:setAreaSpread('normal', 6, 6)
        self.psystem:setColors(251, 242, 54, 75, 251, 242, 54, 50)
    end
end

function Tile:update(dt)
    self.psystem:update(dt)
end

function Tile:render(x, y)
    
    -- draw shadow
    love.graphics.setColor(34, 32, 52, 255)
    love.graphics.draw(gTextures['main'], gFrames['tiles'][self.color][self.variety],
        self.x + x + 2, self.y + y + 2)

    -- draw tile itself
    love.graphics.setColor(255, 255, 255, 255)
    love.graphics.draw(gTextures['main'], gFrames['tiles'][self.color][self.variety],
        self.x + x, self.y + y)

    if self.shiny then

        love.graphics.draw(self.psystem, self.x + x + 16, self.y + y + 16)
    end
end