import UIKit

class PokemonViewController: UIViewController {
    var url: String!
    var number: String!

    @IBOutlet var nameLabel: UILabel!
    @IBOutlet var numberLabel: UILabel!
    @IBOutlet var type1Label: UILabel!
    @IBOutlet var type2Label: UILabel!
    @IBOutlet var catchButton: UIButton!
    @IBOutlet var spriteView: UIImageView!
    @IBOutlet var descriptionView: UILabel!
    

    func capitalize(text: String) -> String {
        return text.prefix(1).uppercased() + text.dropFirst()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        nameLabel.text = ""
        numberLabel.text = ""
        type1Label.text = ""
        type2Label.text = ""

        loadPokemon()
    }
    
    @IBAction func toggleCatch() {
        if catchButton.currentTitle == "CATCH"{
            self.catchButton.setTitle("RELEASE", for: .normal)
            UserDefaults.standard.set(true, forKey: self.nameLabel.text!)
        }
        else{
            self.catchButton.setTitle("CATCH", for: .normal)
            UserDefaults.standard.set(false, forKey: self.nameLabel.text!)
        }
    }
    
    func processed(text: String) -> String{
        var newtext = text.replacingOccurrences(of: "\n", with: " ")
        newtext = newtext.replacingOccurrences(of: "\u{000C}", with: " ")
        return newtext
    }

    func loadPokemon() {
        URLSession.shared.dataTask(with: URL(string: url)!) { (data, response, error) in
            guard let data = data else {
                return
            }

            do {
                let result = try JSONDecoder().decode(PokemonResult.self, from: data)
                DispatchQueue.main.async {
                    self.navigationItem.title = self.capitalize(text: result.name)
                    self.nameLabel.text = self.capitalize(text: result.name)
                    self.number = String(result.id)
                    self.numberLabel.text = String(format: "#%03d", result.id)
                    
                    let caught = UserDefaults.standard.bool(forKey: self.nameLabel.text!)
                    if caught{
                        self.catchButton.setTitle("RELEASE", for: .normal)
                    }

                    for typeEntry in result.types {
                        if typeEntry.slot == 1 {
                            self.type1Label.text = typeEntry.type.name
                        }
                        else if typeEntry.slot == 2 {
                            self.type2Label.text = typeEntry.type.name
                        }
                    }
                    
                    let descurl = "https://pokeapi.co/api/v2/pokemon-species/" + self.number
                    
                    URLSession.shared.dataTask(with: URL(string: descurl)!) { (data, response, error) in
                        guard let data = data else {
                            return
                        }

                        do {
                            let result = try JSONDecoder().decode(struct1.self, from: data)
                            DispatchQueue.main.async {
                                for text in result.flavor_text_entries{
                                    if text.language.name == "en"{
                                        self.descriptionView.text = self.processed(text: text.flavor_text)
                                        break
                                    }
                                }
                            }
                        }
                        catch let error {
                            print(error)
                        }
                    }.resume()
                    
                    let url = URL(string: result.sprites.front_default!)
                    do{
                        let data = try Data(contentsOf: url!)
                        self.spriteView.image = UIImage(data: data)
                    }
                    catch let error{
                        print(error)
                    }
                }
            }
            catch let error {
                print(error)
            }
        }.resume()
    }
}
