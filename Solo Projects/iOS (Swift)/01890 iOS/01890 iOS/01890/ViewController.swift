//
//  ViewController.swift
//  01890
//
//  Created by Tony D Jones on 6/8/20.
//  Copyright © 2020 Tony D Jones. All rights reserved.
//

import UIKit
import MessageUI
import SwiftCSV

class ViewController: UIViewController, MFMessageComposeViewControllerDelegate, MFMailComposeViewControllerDelegate, UITextViewDelegate {
    
    //These two functions allow you to dismiss the SMS and Mail composers when they are automatically pulled up.
    func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
        controller.dismiss(animated: true, completion: nil)
    }
    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        controller.dismiss(animated: true, completion: nil)
    }
    
    //This function is used to call actions when certain textviews are clicked. Used in the tips page.
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange, interaction: UITextItemInteraction) -> Bool {
        let result = process(input: URL.absoluteString)
        let label = UIButton()
        label.setTitle(result![1], for: .normal)
        if result![0] == "dial"{
            dial(sender: label)
        }
        else if result![0] == "text"{
            text(sender: label)
        }
        return false
    }
    
    //Gives us access to elements of the storyboard: the scrollview, which is pinned to the edges of the screen, the titlebar, and the buttons on the titlebar
    @IBOutlet var scroll: UIScrollView!
    @IBOutlet var titlebar: UINavigationItem!
    @IBOutlet var backbutton: UIBarButtonItem!
    @IBOutlet var searchbutton: UIBarButtonItem!
    @IBOutlet var aboutbutton: UIBarButtonItem!
    
    //Gives global access to a content view, which will alow us to add and remove from the view in multiple functions. This is the main view that will be populated with information and will rest within the scrollview.
    var content: UIStackView!
    
    //default space amount
    var space: CGFloat = 10
    
    //create empty array for Info objects.
    var information: [Info] = []
    
    //A layout constraint globally available. This wil allow us to toggle the restraint on and off for when we want the content view to be locked to a particular height (which will prevent scrolling.)
    var contentheight: NSLayoutConstraint!
    
    //Bool for whether or not the textline is currently active, and to keep track of the phone number
    var textline: Bool?
    var tipnumber: String?
    
    //Dictionary to keep track of pages and sections
    var pages: Dictionary<String, [String]?> = [:]
    
    //Dictionary to keep track of sections and images
    var sections: Dictionary<String, String?> = [:]
    
    //Dictionary to keep track of sections and subsections
    var subsections: Dictionary<String, [String]?> = [:]
    
    //string to keep track of which socil media button to use, will be calculated later
    var socialmediabutton: String!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //set actions of title bar buttons.
        backbutton.action = #selector(reboot)
        backbutton.target = self
        
        aboutbutton.action = #selector(aboutActivity)
        aboutbutton.target = self
        
        searchbutton.action = #selector(searchActivity)
        searchbutton.target = self
        
        //Creates vertical Stackview to be the main holder of our content.
        content = UIStackView()
        content.axis = NSLayoutConstraint.Axis.vertical
        content.spacing = space
        
        //Keeps autolayout from messing with my view boundaries
        content.translatesAutoresizingMaskIntoConstraints = false
        scroll.translatesAutoresizingMaskIntoConstraints = false
        
        //Add content view to the screen.
        scroll.addSubview(content)
        
        //Pin top, left, right sides of content view to scrollview. and sets constant width. This wil allow for easy scrolling with the procedural generation.
        content.widthAnchor.constraint(equalTo: scroll.widthAnchor).isActive = true
        content.leadingAnchor.constraint(equalTo: scroll.leadingAnchor).isActive = true
        content.trailingAnchor.constraint(equalTo: scroll.trailingAnchor).isActive = true
        content.topAnchor.constraint(equalTo: scroll.topAnchor).isActive = true
        content.bottomAnchor.constraint(equalTo: scroll.bottomAnchor).isActive = true
        
        //set margins of content view.
        content.isLayoutMarginsRelativeArrangement = true
        content.layoutMargins = UIEdgeInsets(top: space, left: space, bottom: space, right: space)
        
        //Set contentheight constraint, BUT NOT YET ACTIVE
        contentheight = content.heightAnchor.constraint(equalTo: scroll.heightAnchor)
        
        //Calculate screen aspect ratio
        let ratio = UIScreen.main.bounds.height / UIScreen.main.bounds.width
        let thin = abs(ratio - (4.0 / 3.0))
        let normal = abs(ratio - (8.0 / 5.0))
        let wide = abs(ratio - (16.0 / 9.0))
        
        //determine which social media button to use based on aspect ratio
        if thin < normal && thin < wide {
            socialmediabutton = "socialmediav1.png"
        }
        else if wide < thin && wide < normal {
            socialmediabutton = "socialmediav3.png"
        }
        else{
            socialmediabutton = "socialmediav2.png"
        }
        
        //load Info objects into information array
        do {
            // From a file inside the app bundle, with a custom delimiter, errors, and custom encoding
            let data: CSV? = try CSV(
                name: "information",
                extension: "csv",
                bundle: .main,
                delimiter: ",",
                encoding: .utf8)!
            for row in data!.enumeratedRows{
                var info = Info()
                info.reference = row[0]
                info.number = process(input: row[1])
                info.text = process(input: row[2])
                info.url = process(input: row[3])
                info.email = process(input: row[4])
                info.page = process(input: row[5])
                info.section = process(input: row[6])
                if row[7].count > 0{
                    info.description = row[7]
                }
                information.append(info)
            }
            
            //Check tipline number and whether it is text or call
            let tipline: CSV? = try CSV(
                name: "tipline",
                extension: "csv",
                bundle: .main,
                delimiter: ",",
                encoding: .utf8)
            if tipline?.enumeratedRows[0][0].uppercased() == "CALL"{
                textline = false
            }
            else{
                textline = true
            }
            tipnumber = tipline?.enumeratedRows[0][1]
            
            //make dictionary connecting pages to sections
            let pagecsv: CSV? = try CSV(
                name: "pages",
                extension: "csv",
                bundle: .main,
                delimiter: ",",
                encoding: .utf8)
            
            for row in pagecsv!.enumeratedRows{
                pages[row[0]] = process(input: row[1])
            }
            
            //populate dictionaries connecting sections to images and sections to subsections
            let sectioncsv: CSV? = try CSV(
                name: "sections",
                extension: "csv",
                bundle: .main,
                delimiter: ",",
                encoding: .utf8)
            for row in sectioncsv!.enumeratedRows{
                if row[1].count > 0{
                    sections[row[0]] = row[1]
                }
                if row[2].count > 0{
                    subsections[row[0]] = process(input: row[2])
                }
            }
        } catch {
        }
        
        //Open home page
        boot()
    }
    
    
    func boot(){
        
        //clear views from base and lock content view height.
        content.subviews.forEach({$0.removeFromSuperview()})
        content.distribution  = UIStackView.Distribution.fillEqually
        contentheight.isActive = true
        
        //Remove back button and add about button and search buton to titlebar.
        titlebar.setLeftBarButton(nil, animated: false)
        titlebar.setRightBarButtonItems([aboutbutton,searchbutton], animated: false)
        titlebar.title = "01890"
        
        //Below the code generates rows of buttons. I'll comment this first one but the ones after are basically the same.
        
        //Horizontal stackview.
        let row1 = UIStackView()
        row1.axis = NSLayoutConstraint.Axis.horizontal
        row1.distribution  = UIStackView.Distribution.fillEqually
        row1.alignment = UIStackView.Alignment.fill
        row1.spacing = space
        
        //2 buttons created
        let coalition = UIButton()
        coalition.setBackgroundImage(UIImage(named: socialmediabutton), for: .normal)
        coalition.addTarget(self, action: #selector(coalitionActivity), for: .touchUpInside)
        
        //If textline is active, text buttoon will load the text tip line page. Otherwise it will load phone tip line page.
        let tips = UIButton()
        if textline!{
            tips.setBackgroundImage(UIImage(named: "textbutton.png"), for: .normal)
            tips.addTarget(self, action: #selector(tipsActivity), for: .touchUpInside)
        }
        else{
            tips.setBackgroundImage(UIImage(named: "callbutton.png"), for: .normal)
            tips.addTarget(self, action: #selector(anonActivity), for: .touchUpInside)
        }
        
        //row added to screen and buttons added to row
        content.addArrangedSubview(row1)
        row1.addArrangedSubview(tips)
        row1.addArrangedSubview(coalition)
        
        let row2 = UIStackView()
        row2.axis = NSLayoutConstraint.Axis.horizontal
        row2.distribution  = UIStackView.Distribution.fillEqually
        row2.alignment = UIStackView.Alignment.fill
        row2.spacing = space
        
        let contacts = UIButton()
        contacts.setBackgroundImage(UIImage(named: "schools.png"), for: .normal)
        contacts.addTarget(self, action: #selector(contactsActivity), for: .touchUpInside)
        
        let emergency = UIButton()
        emergency.setBackgroundImage(UIImage(named: "emergency.png"), for: .normal)
        emergency.addTarget(self, action: #selector(emergencyActivity), for: .touchUpInside)
        
        content.addArrangedSubview(row2)
        row2.addArrangedSubview(contacts)
        row2.addArrangedSubview(emergency)

        let row3 = UIStackView()
        row3.axis = NSLayoutConstraint.Axis.horizontal
        row3.distribution  = UIStackView.Distribution.fillEqually
        row3.alignment = UIStackView.Alignment.fill
        row3.spacing = space
        
        let mental = UIButton()
        mental.setBackgroundImage(UIImage(named: "mental.png"), for: .normal)
        mental.addTarget(self, action: #selector(mentalActivity), for: .touchUpInside)
        
        let community = UIButton()
        community.setBackgroundImage(UIImage(named: "community.png"), for: .normal)
        community.addTarget(self, action: #selector(communityActivity), for: .touchUpInside)
        
        content.addArrangedSubview(row3)
        row3.addArrangedSubview(mental)
        row3.addArrangedSubview(community)

        let row4 = UIStackView()
        row4.axis = NSLayoutConstraint.Axis.horizontal
        row4.distribution  = UIStackView.Distribution.fillEqually
        row4.alignment = UIStackView.Alignment.fill
        row4.spacing = space
        
        let articles = UIButton()
        articles.setBackgroundImage(UIImage(named: "articles.png"), for: .normal)
        articles.accessibilityLabel = "winchestercoalitionsafercommunity.com/articles-of-interest"
        articles.addTarget(self, action: #selector(website), for: .touchUpInside)
        
        let school = UIButton()
        school.setBackgroundImage(UIImage(named: "schoolresources.png"), for: .normal)
        school.addTarget(self, action: #selector(schoolActivity), for: .touchUpInside)
        
        content.addArrangedSubview(row4)
        row4.addArrangedSubview(articles)
        row4.addArrangedSubview(school)
    }
    
    @objc func tipsActivity(sender: UIButton!){
        //wipe screen and change title
        setup(title: "Anonymous Reporting")
        
        //normal text at the top, but extra large
        let text = UILabel()
        text.text = "STOP!"
        text.lineBreakMode = .byWordWrapping
        text.numberOfLines = 0
        text.font = text.font.withSize(text.font.pointSize * 6)
        text.textColor = .red
        text.textAlignment = .center
        content.addArrangedSubview(text)
        
        //creates a textview with a red clickable link that sends a string to a function to perform an action
        let text2 = UITextView()
        let attributedOriginalText = NSMutableAttributedString(string: "If there is an EMERGENCY, call 911")
        let linkRange = attributedOriginalText.mutableString.range(of: "call 911")
        attributedOriginalText.addAttribute(.link, value: "dial,911", range: linkRange)
        text2.attributedText = attributedOriginalText
        text2.font = UIFont(name: text2.font!.fontName, size: text.font.pointSize / 3.5)
        text2.linkTextAttributes = [NSAttributedString.Key.foregroundColor:UIColor.red, .underlineStyle: 1]
        let fixedWidth = content.frame.size.width
        let newSize = text2.sizeThatFits(CGSize(width: fixedWidth, height: CGFloat.greatestFiniteMagnitude))
        text2.frame.size = CGSize(width: max(newSize.width, fixedWidth), height: newSize.height)
        text2.isScrollEnabled = false
        text2.textAlignment = .center
        text2.isUserInteractionEnabled = true
        text2.isEditable = false
        text2.delegate = self
        content.addArrangedSubview(text2)
        
        //creates text similar to the call 911 text above, except calling the WPD
        let callbutton = UITextView()
        let attributedOriginalText2 = NSMutableAttributedString(string: "Or call 781-729-1212")
        let linkRange2 = attributedOriginalText2.mutableString.range(of: "call 781-729-1212")
        attributedOriginalText2.addAttribute(.link, value: "dial,781-729-1212", range: linkRange2)
        callbutton.attributedText = attributedOriginalText2
        callbutton.font = UIFont(name: callbutton.font!.fontName, size: text.font.pointSize / 3.5)
        callbutton.linkTextAttributes = [NSAttributedString.Key.foregroundColor:UIColor.red, .underlineStyle: 1]
        let newSize2 = callbutton.sizeThatFits(CGSize(width: fixedWidth, height: CGFloat.greatestFiniteMagnitude))
        callbutton.frame.size = CGSize(width: max(newSize2.width, fixedWidth), height: newSize2.height)
        callbutton.isScrollEnabled = false
        callbutton.textAlignment = .center
        callbutton.isUserInteractionEnabled = true
        callbutton.isEditable = false
        callbutton.delegate = self
        content.addArrangedSubview(callbutton)
        
        //Image 1/3 the width of the screen and centered. Nested stackviews allow us to center the view in the screen.
        let image = UIImageView(image: resizeWidth(image: UIImage(named: "text.png")!, width: Float(content.frame.width) / 3))
        
        let banner = UIStackView()
        banner.axis = NSLayoutConstraint.Axis.vertical
        banner.alignment = UIStackView.Alignment.center
        banner.distribution = .equalSpacing
        let inner = UIStackView()
        inner.axis = NSLayoutConstraint.Axis.horizontal
        inner.alignment = UIStackView.Alignment.center
        inner.distribution = .equalSpacing
        
        content.addArrangedSubview(banner)
        banner.addArrangedSubview(inner)
        inner.addArrangedSubview(image)
        
        //Some text labels...
        let text3 = UILabel()
        text3.text = "Otherwise..."
        text3.lineBreakMode = .byWordWrapping
        text3.numberOfLines = 0
        text3.font = text3.font.withSize(text3.font.pointSize * 1.5)
        text3.textAlignment = .center
        text3.font = UIFont.boldSystemFont(ofSize: text3.font.pointSize)
        content.addArrangedSubview(text3)
        
        //These labels will have part of their text in red
        let text5 = UILabel()
        let stringValue = "If you SEE something..."
        let attributedString: NSMutableAttributedString = NSMutableAttributedString(string: stringValue)
        let range = attributedString.mutableString.range(of: "SEE");
        attributedString.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.red, range: range);
        text5.attributedText = attributedString
        text5.lineBreakMode = .byWordWrapping
        text5.numberOfLines = 0
        text5.textAlignment = .center
        text5.font = text5.font.withSize(text5.font.pointSize * 1.5)
        content.addArrangedSubview(text5)
        
        let text6 = UILabel()
        let stringValue2 = "SAY something..."
        let attributedString2: NSMutableAttributedString = NSMutableAttributedString(string: stringValue2)
        let range2 = attributedString2.mutableString.range(of: "SAY");
        attributedString2.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.red, range: range2);
        text6.attributedText = attributedString2
        text6.lineBreakMode = .byWordWrapping
        text6.numberOfLines = 0
        text6.textAlignment = .center
        text6.font = text6.font.withSize(text6.font.pointSize * 1.5)
        content.addArrangedSubview(text6)
        
        let text7 = UILabel()
        let stringValue3 = "DO something."
        let attributedString3: NSMutableAttributedString = NSMutableAttributedString(string: stringValue3)
        let range3 = attributedString3.mutableString.range(of: "DO");
        attributedString3.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.red, range: range3);
        text7.attributedText = attributedString3
        text7.lineBreakMode = .byWordWrapping
        text7.numberOfLines = 0
        text7.textAlignment = .center
        text7.font = text7.font.withSize(text7.font.pointSize * 1.5)
        content.addArrangedSubview(text7)
        
        let text8 = UILabel()
        let stringValue4 = "Report anonymously to the Winchester Police Department"
        let attributedString4: NSMutableAttributedString = NSMutableAttributedString(string: stringValue4)
        let range4 = attributedString4.mutableString.range(of: "anonymously");
        attributedString4.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.red, range: range4);
        text8.attributedText = attributedString4
        text8.lineBreakMode = .byWordWrapping
        text8.numberOfLines = 0
        text8.textAlignment = .center
        text8.font = text8.font.withSize(text8.font.pointSize * 1.5)
        content.addArrangedSubview(text8)
        
        //Horizontal stackview for a textfield (to input text) and a send button
        let search_bar = UIStackView()
        search_bar.axis = .horizontal
        search_bar.distribution = .fillProportionally
        search_bar.spacing = space
        
        content.addArrangedSubview(search_bar)
        
        let edit = UITextField()
        edit.accessibilityLabel = "edit"
        edit.placeholder = "Type your anonymous report here"
        
        //Button calls autotext()
        let button3 = UIButton()
        button3.setTitle("Send", for: .normal)
        button3.setTitleColor(.blue, for: .normal)
        button3.addTarget(self, action: #selector(autotext), for: .touchUpInside)
        
        search_bar.addArrangedSubview(edit)
        search_bar.addArrangedSubview(button3)
        
        //The button will be 1/4 the width of the input field.
        button3.widthAnchor.constraint(equalTo: edit.widthAnchor, multiplier: 0.25).isActive = true
        
        //Extra informations at the bottom of the screen.
        let text11 = UILabel()
        text11.text = "When not using this app, you can text tips by entering the word \"Winchester\" before your message and texting it to " + tipnumber! + ", or calling the Winchester Police Department at 781-729-1212."
        text11.lineBreakMode = .byWordWrapping
        text11.numberOfLines = 0
        content.addArrangedSubview(text11)
        
        let text0 = UILabel()
        text0.text = "Example: \"Winchester, there is a party planned at 123 xxxx ST this Saturday night, I believe the parents are away.\""
        text0.lineBreakMode = .byWordWrapping
        text0.numberOfLines = 0
        content.addArrangedSubview(text0)
    }
    
    //See the tips activity() function for most of the same comments. I will only note differences.
    @objc func anonActivity(sender: UIButton!){
        setup(title: "Anonymous Reporting")
        
        let text = UILabel()
        text.text = "STOP!"
        text.lineBreakMode = .byWordWrapping
        text.numberOfLines = 0
        text.font = text.font.withSize(text.font.pointSize * 6)
        text.textColor = .red
        text.textAlignment = .center
        content.addArrangedSubview(text)
        
        let text2 = UITextView()
        let attributedOriginalText = NSMutableAttributedString(string: "If there is an EMERGENCY, call 911")
        let linkRange = attributedOriginalText.mutableString.range(of: "call 911")
        attributedOriginalText.addAttribute(.link, value: "dial,911", range: linkRange)
        text2.attributedText = attributedOriginalText
        text2.font = UIFont(name: text2.font!.fontName, size: text.font.pointSize / 3.5)
        text2.linkTextAttributes = [NSAttributedString.Key.foregroundColor:UIColor.red, .underlineStyle: 1]
        let fixedWidth = content.frame.size.width
        let newSize = text2.sizeThatFits(CGSize(width: fixedWidth, height: CGFloat.greatestFiniteMagnitude))
        text2.frame.size = CGSize(width: max(newSize.width, fixedWidth), height: newSize.height)
        text2.isScrollEnabled = false
        text2.textAlignment = .center
        text2.isUserInteractionEnabled = true
        text2.isEditable = false
        text2.delegate = self
        content.addArrangedSubview(text2)
        
        let image = UIImageView(image: resizeWidth(image: UIImage(named: "telephone.png")!, width: Float(content.frame.width) / 3))
        
        let banner = UIStackView()
        banner.axis = NSLayoutConstraint.Axis.vertical
        banner.alignment = UIStackView.Alignment.center
        banner.distribution = .equalSpacing
        let inner = UIStackView()
        inner.axis = NSLayoutConstraint.Axis.horizontal
        inner.alignment = UIStackView.Alignment.center
        inner.distribution = .equalSpacing
        
        content.addArrangedSubview(banner)
        banner.addArrangedSubview(inner)
        inner.addArrangedSubview(image)
        
        let text3 = UILabel()
        text3.text = "Otherwise..."
        text3.lineBreakMode = .byWordWrapping
        text3.numberOfLines = 0
        text3.font = text3.font.withSize(text3.font.pointSize * 1.5)
        text3.textAlignment = .center
        text3.font = UIFont.boldSystemFont(ofSize: text3.font.pointSize)
        content.addArrangedSubview(text3)
        
        let text5 = UILabel()
        let stringValue = "If you SEE something..."
        let attributedString: NSMutableAttributedString = NSMutableAttributedString(string: stringValue)
        let range = attributedString.mutableString.range(of: "SEE");
        attributedString.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.red, range: range);
        text5.attributedText = attributedString
        text5.lineBreakMode = .byWordWrapping
        text5.numberOfLines = 0
        text5.textAlignment = .center
        text5.font = text5.font.withSize(text5.font.pointSize * 1.5)
        content.addArrangedSubview(text5)
        
        let text6 = UILabel()
        let stringValue2 = "SAY something..."
        let attributedString2: NSMutableAttributedString = NSMutableAttributedString(string: stringValue2)
        let range2 = attributedString2.mutableString.range(of: "SAY");
        attributedString2.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.red, range: range2);
        text6.attributedText = attributedString2
        text6.lineBreakMode = .byWordWrapping
        text6.numberOfLines = 0
        text6.textAlignment = .center
        text6.font = text6.font.withSize(text6.font.pointSize * 1.5)
        content.addArrangedSubview(text6)
        
        let text7 = UILabel()
        let stringValue3 = "DO something."
        let attributedString3: NSMutableAttributedString = NSMutableAttributedString(string: stringValue3)
        let range3 = attributedString3.mutableString.range(of: "DO");
        attributedString3.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.red, range: range3);
        text7.attributedText = attributedString3
        text7.lineBreakMode = .byWordWrapping
        text7.numberOfLines = 0
        text7.textAlignment = .center
        text7.font = text7.font.withSize(text7.font.pointSize * 1.5)
        content.addArrangedSubview(text7)
        
        let text8 = UILabel()
        let stringValue4 = "Report anonymously to the Winchester Police Department"
        let attributedString4: NSMutableAttributedString = NSMutableAttributedString(string: stringValue4)
        let range4 = attributedString4.mutableString.range(of: "anonymously");
        attributedString4.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.red, range: range4);
        text8.attributedText = attributedString4
        text8.lineBreakMode = .byWordWrapping
        text8.numberOfLines = 0
        text8.textAlignment = .center
        text8.font = text8.font.withSize(text8.font.pointSize * 1.5)
        content.addArrangedSubview(text8)
        
        //creates text similar to the call 911 text above, except highlighting the tip line number. clicking the number will offer the user to call the number.
        let callbutton = UITextView()
        let attributedOriginalText2 = NSMutableAttributedString(string: "Call " + tipnumber!)
        let linkRange2 = attributedOriginalText2.mutableString.range(of: tipnumber!)
        attributedOriginalText2.addAttribute(.link, value: "dial," + tipnumber!, range: linkRange2)
        callbutton.attributedText = attributedOriginalText2
        callbutton.font = UIFont(name: callbutton.font!.fontName, size: text.font.pointSize / 3.5)
        callbutton.linkTextAttributes = [NSAttributedString.Key.foregroundColor:UIColor.red, .underlineStyle: 1]
        let newSize2 = callbutton.sizeThatFits(CGSize(width: fixedWidth, height: CGFloat.greatestFiniteMagnitude))
        callbutton.frame.size = CGSize(width: max(newSize2.width, fixedWidth), height: newSize2.height)
        callbutton.isScrollEnabled = false
        callbutton.textAlignment = .center
        callbutton.isUserInteractionEnabled = true
        callbutton.isEditable = false
        callbutton.delegate = self
        content.addArrangedSubview(callbutton)
    }
    
    @objc func coalitionActivity(sender: UIButton!){
        
        //Clear screen and set up base and title
        setup(title: "Winchester Coalition For A Safer Community")
        
        //horizontal stackview with coalition image on the left and informational text on the right
        let banner = UIStackView()
        banner.axis = .horizontal
        banner.distribution = .fill
        banner.alignment = .center
        banner.spacing = space
        content.addArrangedSubview(banner)
        
        let image = UIImageView(image: resizeWidth(image: UIImage(named: "coalition.jpg")!, width: Float(content.frame.width * 2 / 5)))
        banner.addArrangedSubview(image)
        image.widthAnchor.constraint(equalTo: banner.widthAnchor, multiplier: 0.4).isActive = true
        
        let text = UILabel()
        text.text = "The Winchester Coalition For A Safer Community, housed within the Winchester Health Department, is a comprehensive community-based organization that works collaboratively with residents, town departments, and agencies to reduce risky behaviors, particularly in the youth community, and to foster healthy life choices through education."
        text.lineBreakMode = .byWordWrapping
        text.numberOfLines = 0
        banner.addArrangedSubview(text)
        
        //Horizontal StackView of 2 buttons. One button goes to coalition website, other goes to coalition facebook page.
        let stack = UIStackView()
        stack.axis = .horizontal
        stack.distribution = .fillEqually
        stack.spacing = space
        content.addArrangedSubview(stack)
        
        let button = UIButton()
        button.setTitle("Visit Website", for: .normal)
        button.setTitleColor(.blue, for: .normal)
        button.titleLabel?.lineBreakMode = .byWordWrapping
        button.accessibilityLabel = "winchestercoalitionsafercommunity.com"
        button.addTarget(self, action: #selector(website), for: .touchUpInside)
        stack.addArrangedSubview(button)
        
        let button2 = UIButton()
        button2.setTitle("Facebook Page", for: .normal)
        button2.setTitleColor(.blue, for: .normal)
        button2.titleLabel?.lineBreakMode = .byWordWrapping
        button2.accessibilityLabel = "facebook.com/Winchester.Coalition.Safer.Community"
        button2.addTarget(self, action: #selector(website), for: .touchUpInside)
        stack.addArrangedSubview(button2)
    }
    
    //Pretty much all of the functions to load pages work on this basic outline. I'll describe it here and won't comment on further similar functions. I'll note differences.
    @objc func contactsActivity(sender: UIButton!){
        
        //Clear screen and set up base
        setup(title: "Non-Emergency Contacts")
        
        //generate sections of the page, to which information can be added
        createSections(page: "Non-Emergency Contacts")
        
        //populate screen with relevant information
        showinfo(activity: "Non-Emergency Contacts")
        
        //button at the bottom of the screen that takes user to winchester town website
        let button = UIButton()
        button.setTitle("For additional town information click here", for: .normal)
        button.setTitleColor(.blue, for: .normal)
        button.accessibilityLabel = "winchester.us/directory.aspx"
        button.addTarget(self, action: #selector(website), for: .touchUpInside)
        content.addArrangedSubview(button)
    }
    
    @objc func emergencyActivity(sender: UIButton!){
        setup(title: "Emergency Contacts")
        
        //generate sections of the page, to which information can be added
        createSections(page: "Emergency Contacts")
        
        //populate screen with relevant information
        showinfo(activity: "Emergency Contacts")
    }
    
    @objc func mentalActivity(sender: UIButton!){
        setup(title: "Mental Health Resources")
        //generate sections of the page, to which information can be added
        createSections(page: "Mental Health")
        
        //populate screen with relevant information
        showinfo(activity: "Mental Health")
        
        //button at bottom of screen takes to designated website.
        let button = UIButton()
        button.setTitle("More support websites and articles", for: .normal)
        button.setTitleColor(.blue, for: .normal)
        button.accessibilityLabel = "jfleming89.wixsite.com/whs-transition"
        button.addTarget(self, action: #selector(website), for: .touchUpInside)
        content.addArrangedSubview(button)
    }
    
    @objc func communityActivity(sender: UIButton!){
        setup(title: "Community Connector")
        //generate sections of the page, to which information can be added
        createSections(page: "Community Connector")
        
        //populate screen with relevant information
        showinfo(activity: "Community Connector")
    }
    
    @objc func schoolActivity(sender: UIButton!){
        setup(title: "School Resources")
        content.spacing = space * 2
        
        //generate sections of the page, to which information can be added
        createSections(page: "School Resources")
        
        //populate screen with relevant information
        showinfo(activity: "School Resources")
    }
    
    @objc func aboutActivity(sender: UIBarButtonItem!){
        setup(title: "About The App")
        
        //Horizontal stackview with app icon on the left and informational text on the right.
        let banner = UIStackView()
        banner.axis = .horizontal
        banner.distribution = .fill
        banner.alignment = .center
        banner.spacing = space
        content.addArrangedSubview(banner)
        
        let image = UIImageView(image: resizeWidth(image: UIImage(named: "icon.jpg")!, width: Float(content.frame.width * 2 / 5)))
        banner.addArrangedSubview(image)
        image.widthAnchor.constraint(equalTo: banner.widthAnchor, multiplier: 0.4).isActive = true
        
        let text = UILabel()
        text.text = "This app was commissioned by the Winchester Coalition For A Safer Community to serve the community of Winchester, MA by providing helpful resources to the residents. It was originally created by Rosanna Zhang and Quiyue Liu and was recreated by Tony D Jones"
        text.lineBreakMode = .byWordWrapping
        text.numberOfLines = 0
        banner.addArrangedSubview(text)
        
        //button takes you to coalition website
        let button = UIButton()
        button.setTitle("For more information about the coalition, please visit our website", for: .normal)
        button.setTitleColor(.blue, for: .normal)
        button.titleLabel?.lineBreakMode = .byWordWrapping
        button.titleLabel?.textAlignment = .center
        button.accessibilityLabel = "winchestercoalitionsafercommunity.com"
        button.addTarget(self, action: #selector(website), for: .touchUpInside)
        content.addArrangedSubview(button)
    }
    
    @objc func searchActivity(sender: UIBarButtonItem!){
        setup(title: "Search")
        content.spacing = space * 2
        
        //Generates and adds search bar to screen. Consists of text input field and a button
        let search_bar = UIStackView()
        search_bar.axis = .horizontal
        search_bar.distribution = .fillProportionally
        search_bar.spacing = space
        
        content.addArrangedSubview(search_bar)
        
        let edit = UITextField()
        edit.accessibilityLabel = "edit"
        edit.placeholder = "Search terms"
        
        //Button will activate generate() function
        let button = UIButton()
        button.setTitle("Search", for: .normal)
        button.setTitleColor(.blue, for: .normal)
        button.addTarget(self, action: #selector(generate), for: .touchUpInside)
        
        search_bar.addArrangedSubview(edit)
        search_bar.addArrangedSubview(button)
        
        //Button will be 1/4 the width of the text input field
        button.widthAnchor.constraint(equalTo: edit.widthAnchor, multiplier: 0.25).isActive = true
    }
    
    @objc func generate(sender: UIButton){
        
        //Have to find the view. Luckily the textfield is labeled, allowing us to search the screen for it so we can pull its text (the user input)
        var search: String! = ""
        for view in content.subviews{
            for subview in view.subviews{
                if subview.accessibilityLabel == "edit"{
                    let edit = subview as! UITextField
                    search = edit.text
                    break
                }
            }
            
        }
        
        //Split search terms into individual words, split by space characters
        let params = search.components(separatedBy: " ")
        
        //clear the screen.
        setup(title: "Search")
        
        //since the screen was cleared, we have to add the search bar back to the screen.
        let search_bar = UIStackView()
        search_bar.axis = .horizontal
        search_bar.distribution = .fillProportionally
        search_bar.spacing = space
        
        content.addArrangedSubview(search_bar)
        
        let edit = UITextField()
        edit.accessibilityLabel = "edit"
        edit.text = search
        
        let button = UIButton()
        button.setTitle("Search", for: .normal)
        button.setTitleColor(.blue, for: .normal)
        button.addTarget(self, action: #selector(generate), for: .touchUpInside)
        
        search_bar.addArrangedSubview(edit)
        search_bar.addArrangedSubview(button)
        
        button.widthAnchor.constraint(equalTo: edit.widthAnchor, multiplier: 0.25).isActive = true
        
        //Array for results of search
        var results: [Info] = []
        
        //Scan the information array for matches of the search terms
        for info in information{
            
            //searching boolean keeps track of whether we are still scanning the current Info object for a match. Saves time and prevents redundancy in results.
            var searching: Bool = true
            if match(source: info.reference!, params: params){
                results.append(info)
                searching = false
            }
            if searching && info.email != nil{
                for email in info.email!{
                    if match(source: email, params: params){
                        results.append(info)
                        searching = false
                        break
                    }
                }
            }
            if searching && info.text != nil{
                for text in info.text!{
                    if match(source: text, params: params){
                        results.append(info)
                        searching = false
                        break
                    }
                }
            }
            if searching && info.description != nil && match(source: info.description!, params: params){
                results.append(info)
                searching = false
            }
            if searching && info.number != nil{
                for number in info.number!{
                    if match(source: number, params: params){
                        results.append(info)
                        searching = false
                        break
                    }
                }
            }
            if searching && info.url != nil{
                for number in info.url!{
                    if match(source: number, params: params){
                        results.append(info)
                        searching = false
                        break
                    }
                }
            }
        }
        
        //For every matched result...
        for result in results{
            
            //present the information. We need to keep note of the returned stackview so we can add the page button to it.
            let stack = makeviews(result: result)
            
            //Generate an additional button that will redirect user to the Info's main page.
            let page = UIButton()
            page.setTitle("Go To Page", for: .normal)
            page.setTitleColor(.blue, for: .normal)
            page.accessibilityLabel = result.page![0]
            page.addTarget(self, action: #selector(redirect), for: .touchUpInside)
            
            //if stack is not nil, add the page button to to view. otherwise, generate a stackview to add the page button to.
            if stack != nil{
                stack!.addArrangedSubview(page)
            }
            else{
                let newstack = UIStackView()
                newstack.axis = NSLayoutConstraint.Axis.vertical
                newstack.alignment = UIStackView.Alignment.trailing
                newstack.spacing = space
                newstack.distribution = .fill
                content.addArrangedSubview(newstack)
                newstack.addArrangedSubview(page)
            }
        }
    }
    
    //Called by the back button in the titlebar. Reloads the home page.
    @objc func reboot(sender: UIBarButtonItem!){
        content.spacing = space
        boot()
    }
    
    //function that clears the screen and changes the title. Also adds the backbutton and removes the search and about buttons.
    func setup(title: String){
        content.subviews.forEach({$0.removeFromSuperview()})
        contentheight.isActive = false
        content.distribution = .equalSpacing
        
        titlebar.title = title
        titlebar.setLeftBarButton(backbutton, animated: false)
        titlebar.setRightBarButtonItems(nil, animated: false)
    }
    
    //Function resizes UIImage to a reasonable size, based on specified height. Usually a percentage of the height of a neighboring view. Scales both height and width.
     func resizeHeight(image: UIImage, height: Float) -> UIImage {
        let size = image.size

        //Calculate ratio of current height to target height
        let ratio  = height / (Float(size.height))
        
        //Create new size parameters based on calculated ratio.
        var newSize: CGSize
        newSize = CGSize(width: size.width * CGFloat(ratio), height: size.height * CGFloat(ratio))

        // Make Rect using new size parameters
        let rect = CGRect(x: 0, y: 0, width: newSize.width, height: newSize.height)

        // Actually do the resizing to the rect
        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        image.draw(in: rect)
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        //Return resized image
        return newImage!
    }
    
    //This function does basically the same thing, but resizes using a width constraint instead of height. For when we want the image to take up a certain width on the screen (as in a horizontal stack) rather than a certain height (As in a vertical stackview)
    func resizeWidth(image: UIImage, width: Float) -> UIImage {
        let size = image.size

        let ratio  = width / (Float(size.width))
        
        // Figure out what our orientation is, and use that to form the rectangle
        var newSize: CGSize
        newSize = CGSize(width: size.width * CGFloat(ratio), height: size.height * CGFloat(ratio))

        // This is the rect that we've calculated out and this is what is actually used below
        let rect = CGRect(x: 0, y: 0, width: newSize.width, height: newSize.height)

        // Actually do the resizing to the rect using the ImageContext stuff
        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        image.draw(in: rect)
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return newImage!
    }
    
    //function to add information to content view
    func showinfo(activity: String){
        
        //Checks all Info in information array to see if Info should be displayed on this page.
        var results: [Info] = []
        for info in information{
            if info.page!.contains(activity){
                results.append(info)
            }
        }
        
        //Add Info to the content view
        for result in results{
            makeviews(result: result)
        }
    }
    
    //for search funtion, detects if source string contains all of the search words
    func match(source: String, params: [String]) -> Bool{
        
        //counts number of matches
        var matches = 0
        for word in params{
            if source.uppercased().contains(word.uppercased()){
                
                //If word is in source, increment match counter
                matches = matches + 1
            }
        }
        
        //if the source has all of the search terms, return true, it's a match
        if matches == params.count{
            return true
        }
        return false
    }
    
    //This function give the user the option to call a phone number
    @objc func dial(sender: UIButton){
        
        //dictionary to translate letters into numbers to make a valid phone number
        let translator: [String:[String]] = ["0":[],
                                             "1":[],
                                             "2":["A","B","C"],
                                             "3":["D","E","F"],
                                             "4":["G","H","I"],
                                             "5":["J","K","L"],
                                             "6":["M","N","O"],
                                             "7":["P","Q","R","S"],
                                             "8":["T","U","V"],
                                             "9":["W","X","Y","Z"]]
        
        //Get data from sender. May be the titleLabel, but if the button has an accessibilityLabel, it will override the titleLabel
        var initial = sender.titleLabel?.text
        if sender.accessibilityLabel != nil{
            initial = sender.accessibilityLabel
        }
        
        //US area code +1
        var number = "1"
        
        //for every character i the initial number, run through translator. hyphens will be ignored, numbers will be dded, letters will be converted to their corresponding number then added.
        for char in initial!{
            let digit = String(char)
            if translator.keys.contains(digit){
                number = number + digit
            }
            else{
                for key in translator.keys{
                    if translator[key]!.contains(digit){
                        number = number + digit
                        break
                    }
                }
            }
        }
        
        //Present option to call the number.
        UIApplication.shared.open(URL(string: "tel://" + number)!)
        
    }
    
    //Opens an SMS message composer. Pulls data from the sender button to determine the destination number.
    @objc func text(sender: UIButton){
        let message = MFMessageComposeViewController()
        message.messageComposeDelegate = self
        message.recipients = ["1" + (sender.titleLabel?.text)!]
        self.present(message, animated: true, completion: nil)
    }
    
    //Opens an email message composer. Pulls data from the sender button to determine the destination email address.
    @objc func mail(sender: UIButton){
        let message = MFMailComposeViewController()
        message.mailComposeDelegate = self
        message.setToRecipients([sender.titleLabel!.text!])
        self.present(message, animated: true, completion: nil)
    }
    
    //Opens default browser to target URL. separte functions to ensure compatibility with older iOS versions.
    @objc func website(sender: UIButton){
        if #available(iOS 10.0, *){
            UIApplication.shared.open(URL(string: "https://www." + sender.accessibilityLabel!)!)
        }
        else{
            UIApplication.shared.openURL(URL(string: "https://www." + sender.accessibilityLabel!)!)
        }
    }
    
    //Opens an SMS message composer pre-populated with text from a Textfield
    @objc func autotext(sender: UIButton){
        var search: String! = ""
        
        //The textview is labeled, search the content view to find it and pull the text from it.
        for view in content.subviews{
            for subview in view.subviews{
                if subview.accessibilityLabel == "edit"{
                    let edit = subview as! UITextField
                    search = edit.text
                    break
                }
            }
            
        }
        
        //Pull up SMS message composer with text from the textfield.
        let message = MFMessageComposeViewController()
        message.messageComposeDelegate = self
        message.body = "Winchester, " + search
        
        //This is the destination number. If the textline is ever active again, but the number changes, this number will also need to change.
        message.recipients = [tipnumber!]
        self.present(message, animated: true, completion: nil)
    }
    
    //This function takes a string and determines whih page to load.
    @objc func redirect(sender: UIButton){
        let page = sender.accessibilityLabel
        if page?.lowercased() == "tips"{
            tipsActivity(sender: nil)
        }
        else if page == "Non-Emergency Contacts"{
            contactsActivity(sender: nil)
        }
        else if page?.lowercased() == "coalition"{
            coalitionActivity(sender: nil)
        }
        else if page == "Emergency Contacts"{
            emergencyActivity(sender: nil)
        }
        else if page == "Mental Health"{
            mentalActivity(sender: nil)
        }
        else if page == "Community Connector"{
            communityActivity(sender: nil)
        }
        else if page == "School Resources"{
            schoolActivity(sender: nil)
        }
    }
    
    //Function to make informational views and add them to the content view. uses an Info object
    func makeviews(result: Info) -> UIStackView?{
        
        //label for the reference string, added to the proper section per the Info's section array.
        let categoryview = find(result: result)
        
        let view = UIStackView()
        view.axis = NSLayoutConstraint.Axis.vertical
        view.spacing = space / 3
        view.distribution = .fill
        categoryview.addArrangedSubview(view)
        
        let reference = UILabel()
        reference.text = result.reference
        reference.lineBreakMode = NSLineBreakMode.byWordWrapping
        reference.numberOfLines = 0
        view.addArrangedSubview(reference)
        
        //Label for the description if not null
        if result.description != nil{
            let description = UILabel()
            description.lineBreakMode = NSLineBreakMode.byWordWrapping
            description.numberOfLines = 0
            description.textColor = .gray
            description.text = result.description
            view.addArrangedSubview(description)
        }
        
        //if more information to be displayed, generate a stackview to which we can add the contct information.
        if result.number != nil || result.text != nil || result.email != nil || result.url != nil{
            
            //Configure StackView to align buttons to the right side of the screen.
            let stack = UIStackView()
            stack.axis = NSLayoutConstraint.Axis.vertical
            stack.alignment = UIStackView.Alignment.trailing
            stack.spacing = space / 3
            stack.distribution = .fill
            view.addArrangedSubview(stack)
            
            //buttons for phone numbers
            if result.number != nil{
                for num in result.number!{
                    let number = UIButton()
                    number.setTitle(num, for: .normal)
                    number.setTitleColor(.blue, for: .normal)
                    number.titleLabel?.lineBreakMode = NSLineBreakMode.byWordWrapping
                    number.addTarget(self, action: #selector(dial), for: .touchUpInside)
                    stack.addArrangedSubview(number)
                }
            }
            
            //button for text number
            if result.text != nil{
                for num in result.text!{
                    let number = UIButton()
                    number.setTitle(num, for: .normal)
                    number.setTitleColor(.blue, for: .normal)
                    number.titleLabel?.lineBreakMode = NSLineBreakMode.byWordWrapping
                    number.addTarget(self, action: #selector(text), for: .touchUpInside)
                    stack.addArrangedSubview(number)
                }
            }
            
            //button for email address
            if result.email != nil{
                for num in result.email!{
                    let number = UIButton()
                    number.setTitle(num, for: .normal)
                    number.setTitleColor(.blue, for: .normal)
                    number.titleLabel?.lineBreakMode = NSLineBreakMode.byWordWrapping
                    number.addTarget(self, action: #selector(mail), for: .touchUpInside)
                    stack.addArrangedSubview(number)
                }
            }
            
            //buttons for URLs
            if result.url != nil{
                let urllist: [String]! = result.url
                for url in urllist{
                    let site = UIButton()
                    site.setTitle(url, for: .normal)
                    site.setTitleColor(.blue, for: .normal)
                    site.titleLabel?.lineBreakMode = NSLineBreakMode.byWordWrapping
                    site.contentHorizontalAlignment = .right
                    site.accessibilityLabel = url
                    site.addTarget(self, action: #selector(website), for: .touchUpInside)
                    let height = NSLayoutConstraint(item: site,
                                                   attribute: .height,
                                                   relatedBy: .equal,
                                                   toItem: site.titleLabel,
                                                   attribute: .height,
                                                   multiplier: 1,
                                                   constant: 0)
                    site.addConstraint(height)
                    stack.addArrangedSubview(site)
                }
            }
            //returns the stack. most of the time this isn't used, but is used for generate()
            return stack
        }
        
        //this doesn't matter, but the function has to return something for when generate() uses it. Should never make it this far, unless the Info object has no contact information.
        return nil
    }
    
    //Creates sections on a page
    func createSections(page: String){
        
        //If there are no sections, doesn't add any, but adjust the spacing of the screen
        if pages[page]! == nil{
            content.spacing = space * 5
            return
        }
        
        //increase screen spacing
        content.spacing = space * 2
        
        //For every section that is on this page
        for section in pages[page]!!{
            //generate header for the section
            let text = UILabel()
            text.text = section
            
            //enlarge text
            text.font = text.font.withSize(text.font.pointSize * 1.5)
            
            //nested stackviews, horizontal inside a vertical one. This will allow us to place the image and the text next to each other in the horizontal layout, and then center the horizontal layout within the vertical layout, so the header appears centered.
            let banner = UIStackView()
            banner.axis = NSLayoutConstraint.Axis.vertical
            banner.alignment = UIStackView.Alignment.center
            banner.distribution = .equalSpacing
            let inner = UIStackView()
            inner.axis = NSLayoutConstraint.Axis.horizontal
            inner.alignment = UIStackView.Alignment.center
            inner.distribution = .equalSpacing
            inner.spacing = space
            
            //if there's an image, resize it and add it to the banner along with the title
            if sections[section] != nil{
                let img = UIImage(named: sections[section]!!)
                let image = UIImageView(image: resizeHeight(image: img!, height: Float(text.font.pointSize * 1.4)))
                content.addArrangedSubview(banner)
                banner.addArrangedSubview(inner)
                inner.addArrangedSubview(image)
                inner.addArrangedSubview(text)
            }
                
            //if not image, just add title to banner
            else{
                content.addArrangedSubview(banner)
                banner.addArrangedSubview(inner)
                inner.addArrangedSubview(text)
            }
            
            //This view is for the actual information to be added. Will be placed directly below the header.
            //It has as tag that will allow the find() function to find and return it so Info can be properly added to the content view.
            let stack = UIStackView()
            stack.accessibilityLabel = section
            stack.axis = NSLayoutConstraint.Axis.vertical
            stack.alignment = UIStackView.Alignment.fill
            stack.spacing = space * 5
            content.addArrangedSubview(stack)
            
            //If this section has subsections, generate them
            if subsections[section] != nil{
                createSubsections(section: section)
            }
        }
    }
    
    //This function generates subsections under a section. very similar to createSections function
    func createSubsections(section: String){
        //For every subsection in this section
        for subsection in subsections[section]!!{
            //generate header for the section
            let text = UILabel()
            text.text = subsection
            
            //Slightly larger font size
            text.font = text.font.withSize(text.font.pointSize * 1.25)
            content.addArrangedSubview(text)
            
            //This view is for the actual information to be added. Will be placed directly below the header.
            //It has as tag that will allow the find() function to find and return it so Info can be properly added to the content view.
            let stack = UIStackView()
            stack.accessibilityLabel = subsection
            stack.axis = NSLayoutConstraint.Axis.vertical
            stack.alignment = UIStackView.Alignment.fill
            stack.spacing = space * 5
            content.addArrangedSubview(stack)
        }
    }
    
    //This function takes the section array of an Info object and finds where on the page that Info should be displayed.
    func find(result: Info) -> UIStackView{
        
        //if no section specified, return the bse linearlayout in the scrollview
        if result.section == nil{
            return content
        }
        
        //If on the current page we find a view with a label that matches one of the sections in the Info's section array, return that view
        let tags: [String]! = result.section
        for tag in tags{
            for view in content.subviews{
                if view.accessibilityLabel == tag && view is UIStackView{
                    return view as! UIStackView
                }
            }
        }
        
        //if no match, the default is the content view
        return content
    }
    
    //this function takes a string and splits it by commas and removes spaces. returns array of srings
    func process(input: String?) -> [String]?{
        //if no string, return nil. Must be nil for app to function correctly, empty string will break the app.
        if input?.count == 0{
            return nil
        }
        
        //Split string by commas and add to array
        var output = input!.components(separatedBy: ",")
        
        //for every string in the output array
        for x in 0..<output.count{
            var string = output[x] as String
            
            //remove spaces from beginning and end of the string
            while string.prefix(1) == " "{
                string = String(string.suffix(string.count - 1))
            }
            while string.suffix(1) == " "{
                string = String(string.prefix(string.count - 1))
            }
            output[x] = string
        }
        return output
    }
}

