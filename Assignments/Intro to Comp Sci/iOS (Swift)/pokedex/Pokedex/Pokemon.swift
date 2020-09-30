import Foundation

struct PokemonListResults: Codable {
    let results: [PokemonListResult]
}

struct PokemonListResult: Codable {
    let name: String
    let url: String
}

struct PokemonResult: Codable {
    let id: Int
    let name: String
    let types: [PokemonTypeEntry]
    let sprites: SpriteDictionary
}

struct PokemonTypeEntry: Codable {
    let slot: Int
    let type: PokemonType
}

struct struct1: Codable {
    let flavor_text_entries: [struct2]
}

struct struct2: Codable{
    let flavor_text: String
    let language: struct3
}

struct struct3: Codable{
    let name: String
}

struct SpriteDictionary: Codable{
    let back_default:String?
    let back_female:String?
    let back_shiny:String?
    let back_shiny_female:String?
    let front_default:String?
    let front_female:String?
    let front_shiny:String?
    let front_shiny_female:String?
}

struct PokemonType: Codable {
    let name: String
}
