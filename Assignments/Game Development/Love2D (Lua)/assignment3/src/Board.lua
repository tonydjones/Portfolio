--[[
    GD50
    Match-3 Remake

    -- Board Class --

    Author: Colton Ogden
    cogden@cs50.harvard.edu

    The Board is our arrangement of Tiles with which we must try to find matching
    sets of three horizontally or vertically.
]]

Board = Class{}

function Board:init(x, y, level)
    self.x = x
    self.y = y
    self.matches = {}
    self.level = level

    self:initializeTiles()
end

function Board:initializeTiles()
    self.tiles = {}

    for tileY = 1, 8 do
        
        -- empty table that will serve as a new row
        table.insert(self.tiles, {})

        for tileX = 1, 8 do
            if self.level == 1 then
                table.insert(self.tiles[tileY], Tile(tileX, tileY, math.random(0,3)*4 + math.random(1,2), 1, 1))
            else
            -- create a new tile at X,Y with a random color and variety
                multiplier = math.random(6)
                table.insert(self.tiles[tileY], Tile(tileX, tileY, math.random(0,3)*4 + math.random(1,2), multiplier, multiplier))
            end
        end
    end

    while self:calculateMatches() or not self:canMatch() do
        
        -- recursively initialize if matches were returned so we always have
        -- a matchless board on start
        self:initializeTiles()
    end
end

--[[
    Goes left to right, top to bottom in the board, calculating matches by counting consecutive
    tiles of the same color. Doesn't need to check the last tile in every row or column if the 
    last two haven't been a match.
]]
function Board:calculateMatches()
    local matches = {}

    -- how many of the same color blocks in a row we've found
    local matchNum = 1

    -- horizontal matches first
    for y = 1, 8 do
        local colorToMatch = self.tiles[y][1].color

        matchNum = 1
        if self.tiles[y][1].shiny then
            shiny = true
        else
            shiny = false
        end

        -- every horizontal tile
        for x = 2, 8 do
            
            -- if this is the same color as the one we're trying to match...
            if self.tiles[y][x].color == colorToMatch then
                matchNum = matchNum + 1
                if self.tiles[y][x].shiny then
                    shiny = true
                end
            else
                
                -- set this as the new color we want to watch for
                colorToMatch = self.tiles[y][x].color

                -- if we have a match of 3 or more up to now, add it to our matches table
                if matchNum >= 3 then
                    local match = {}

                    if shiny then
                        for x2 = 1, 8, 1 do
                            -- add each tile to the match that's in that match
                            table.insert(match, self.tiles[y][x2])
                        end
                    else
                        -- go backwards from here by matchNum
                        for x2 = x - 1, x - matchNum, -1 do
                        
                        -- add each tile to the match that's in that match
                        table.insert(match, self.tiles[y][x2])
                        end
                    end

                    

                    -- add this match to our total matches table
                    table.insert(matches, match)
                end

                matchNum = 1
                shiny = self.tiles[y][x].shiny

                -- don't need to check last two if they won't be in a match
                if x >= 7 then
                    break
                end
            end
        end

        -- account for the last row ending with a match
        if matchNum >= 3 then
            local match = {}
            
            if shiny then
                for x2 = 1, 8, 1 do
                    -- add each tile to the match that's in that match
                    table.insert(match, self.tiles[y][x2])
                end
            else
                -- go backwards from here by matchNum
                for x2 = 9 - 1, 9 - matchNum, -1 do
                
                -- add each tile to the match that's in that match
                table.insert(match, self.tiles[y][x2])
                end
            end

            table.insert(matches, match)
        end
    end

    -- vertical matches
    for x = 1, 8 do
        local colorToMatch = self.tiles[1][x].color

        matchNum = 1
        if self.tiles[1][x].shiny then
            shiny = true
        else
            shiny = false
        end

        -- every vertical tile
        for y = 2, 8 do
            if self.tiles[y][x].color == colorToMatch then
                matchNum = matchNum + 1
                if self.tiles[y][x].shiny then
                    shiny = true
                end
            else
                colorToMatch = self.tiles[y][x].color

                if matchNum >= 3 then
                    local match = {}

                    if shiny then
                        for y2 = 1, 8, 1 do
                            table.insert(match, self.tiles[y2][x])
                        end
                    else
                        for y2 = y - 1, y - matchNum, -1 do
                            table.insert(match, self.tiles[y2][x])
                        end
                    end

                    table.insert(matches, match)
                end

                matchNum = 1
                shiny = self.tiles[y][x].shiny

                -- don't need to check last two if they won't be in a match
                if y >= 7 then
                    break
                end
            end
        end

        -- account for the last column ending with a match
        if matchNum >= 3 then
            local match = {}
            
            if shiny then
                for y2 = 1, 8, 1 do
                    table.insert(match, self.tiles[y2][x])
                end
            else
                for y2 = 9 - 1, 9 - matchNum, -1 do
                    table.insert(match, self.tiles[y2][x])
                end
            end

            table.insert(matches, match)
        end
    end

    -- store matches for later reference
    self.matches = matches

    -- return matches table if > 0, else just return false
    return #self.matches > 0 and self.matches or false
end

--[[
    Remove the matches from the Board by just setting the Tile slots within
    them to nil, then setting self.matches to nil.
]]
function Board:removeMatches()
    for k, match in pairs(self.matches) do
        for k, tile in pairs(match) do
            self.tiles[tile.gridY][tile.gridX] = nil
        end
    end

    self.matches = nil
end

--[[
    Shifts down all of the tiles that now have spaces below them, then returns a table that
    contains tweening information for these new tiles.
]]
function Board:getFallingTiles()
    -- tween table, with tiles as keys and their x and y as the to values
    local tweens = {}

    -- for each column, go up tile by tile till we hit a space
    for x = 1, 8 do
        local space = false
        local spaceY = 0

        local y = 8
        while y >= 1 do
            
            -- if our last tile was a space...
            local tile = self.tiles[y][x]
            
            if space then
                
                -- if the current tile is *not* a space, bring this down to the lowest space
                if tile then
                    
                    -- put the tile in the correct spot in the board and fix its grid positions
                    self.tiles[spaceY][x] = tile
                    tile.gridY = spaceY

                    -- set its prior position to nil
                    self.tiles[y][x] = nil

                    -- tween the Y position to 32 x its grid position
                    tweens[tile] = {
                        y = (tile.gridY - 1) * 32
                    }

                    -- set Y to spaceY so we start back from here again
                    space = false
                    y = spaceY

                    -- set this back to 0 so we know we don't have an active space
                    spaceY = 0
                end
            elseif tile == nil then
                space = true
                
                -- if we haven't assigned a space yet, set this to it
                if spaceY == 0 then
                    spaceY = y
                end
            end

            y = y - 1
        end
    end

    -- create replacement tiles at the top of the screen
    for x = 1, 8 do
        for y = 8, 1, -1 do
            local tile = self.tiles[y][x]

            -- if the tile is nil, we need to add a new one
            if not tile then

                -- new tile with random color and variety
                if self.level == 1 then
                    multiplier = 1
                else
                    -- create a new tile at X,Y with a random color and variety
                    multiplier = math.random(6)
                end

                local tile = Tile(x, y, math.random(0,3)*4 + math.random(1,2), multiplier, multiplier)
                tile.y = -32
                self.tiles[y][x] = tile

                -- create a new tween to return for this tile to fall down
                tweens[tile] = {
                    y = (tile.gridY - 1) * 32
                }
            end
        end
    end

    return tweens
end

function Board:canMatch()
    for a = 1, #self.tiles do
        for b = 1, #self.tiles[1] do
            color = self.tiles[a][b].color
            
            if a > 1 then
                colorUp = self.tiles[a-1][b].color
                self.tiles[a][b].color = colorUp
                self.tiles[a-1][b].color = color
                if not self:calculateMatches() then
                    self.tiles[a][b].color = color
                    self.tiles[a-1][b].color = colorUp
                else
                    self.tiles[a][b].color = color
                    self.tiles[a-1][b].color = colorUp
                    return true
                end
            end

            if a < #self.tiles then
                colorDown = self.tiles[a+1][b].color
                self.tiles[a][b].color = colorDown
                self.tiles[a+1][b].color = color
                if not self:calculateMatches() then
                    self.tiles[a][b].color = color
                    self.tiles[a+1][b].color = colorDown
                else
                    self.tiles[a][b].color = color
                    self.tiles[a+1][b].color = colorDown
                    return true
                end
            end

            if b > 1 then
                colorLeft = self.tiles[a][b-1].color
                self.tiles[a][b].color = colorLeft
                self.tiles[a][b-1].color = color
                if not self:calculateMatches() then
                    self.tiles[a][b].color = color
                    self.tiles[a][b-1].color = colorLeft
                else
                    self.tiles[a][b].color = color
                    self.tiles[a][b-1].color = colorLeft
                    return true
                end
            end

            if b < #self.tiles then
                colorRight = self.tiles[a][b+1].color
                self.tiles[a][b].color = colorRight
                self.tiles[a][b+1].color = color
                if not self:calculateMatches() then
                    self.tiles[a][b].color = color
                    self.tiles[a][b+1].color = colorRight
                else
                    self.tiles[a][b].color = color
                    self.tiles[a][b+1].color = colorRight
                    return true
                end
            end

        end
    end

    return false

end

function Board:render()
    for y = 1, #self.tiles do
        for x = 1, #self.tiles[1] do
            self.tiles[y][x]:render(self.x, self.y)
        end
    end
end

function Board:renderParticles()
    for y = 1, #self.tiles do
        for x = 1, #self.tiles[1] do
            if self.tiles[y][x].shiny then
                self.tiles[y][x].psystem:emit(5)
            end
        end
    end
end