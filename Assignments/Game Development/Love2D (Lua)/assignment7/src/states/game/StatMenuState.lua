--[[
    GD50
    Pokemon

    Author: Colton Ogden
    cogden@cs50.harvard.edu
]]

StatMenuState = Class{__includes = BaseState}

function StatMenuState:init(TakeTurnState, Pokemon)
    self.TakeTurnState = TakeTurnState
    self.pokemon = Pokemon
    self.statMenu = Menu {
        x = VIRTUAL_WIDTH / 2,
        y = (VIRTUAL_HEIGHT - 95) - (VIRTUAL_HEIGHT / 2),
        width = VIRTUAL_WIDTH / 2,
        height = VIRTUAL_HEIGHT / 2,
        stat = true,
        items = {
            {
                text = 'HP: ' ..tostring(self.pokemon.HP - self.TakeTurnState.hp).. ' + ' ..tostring(self.TakeTurnState.hp).. ' = ' ..tostring(self.pokemon.HP),
                onSelect = function()
                    gStateStack:pop()
                    self.TakeTurnState:fadeOutWhite()
                end
            },
            {
                text = 'ATK: ' ..tostring(self.pokemon.attack - self.TakeTurnState.atk).. ' + ' ..tostring(self.TakeTurnState.atk).. ' = ' ..tostring(self.pokemon.attack)
            },
            {
                text = 'DEF: ' ..tostring(self.pokemon.defense - self.TakeTurnState.def).. ' + ' ..tostring(self.TakeTurnState.def).. ' = ' ..tostring(self.pokemon.defense)
            },
            {
                text = 'SPD: ' ..tostring(self.pokemon.speed - self.TakeTurnState.spd).. ' + ' ..tostring(self.TakeTurnState.spd).. ' = ' ..tostring(self.pokemon.speed)
            }
        }
    }
end

function StatMenuState:update(dt)
    self.statMenu:update(dt)
end

function StatMenuState:render()
    self.statMenu:render()
end