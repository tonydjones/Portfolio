--Medal class to determine which medal to render on the score screen

Medal = Class{}

function Medal:init(score, position)
    --Determines medal based on score
    --Making the score thresholds low for testing purposes, but more reasonably 10, 20, and 30
    if score >= 3 then
        self.image = love.graphics.newImage('gold.png')
    elseif score >= 2 then
        self.image = love.graphics.newImage('silver.png')
    elseif score >= 1 then
        self.image = love.graphics.newImage('bronze.png')
    end
    --Render medal in lower center of screen
    
    self.y = VIRTUAL_HEIGHT / 2 - (self.image:getHeight() / 6)

    if position then
        self.x = VIRTUAL_WIDTH / 6 - (self.image:getWidth() / 6)
    else
        self.x = VIRTUAL_WIDTH * 5 / 6 - (self.image:getWidth() / 6)
    end

end


--Render function
function Medal:render()
    love.graphics.draw(self.image, self.x, self.y, 0, 1/3, 1/3)
end