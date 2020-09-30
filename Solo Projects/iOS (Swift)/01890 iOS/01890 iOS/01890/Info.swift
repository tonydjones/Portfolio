//
//  Info.swift
//  01890
//
//  Created by Tony D Jones on 6/9/20.
//  Copyright © 2020 Tony D Jones. All rights reserved.
//

import Foundation

//Made all of the data components optional, although arguably the reference and activity components should be mandatory. But it's working, just have to force things a lot with !
struct Info {
    
    //Organization the information refers to
    var reference: String?
    
    //phone number(s)
    var number: [String]?
    
    //text number
    var text: [String]?
    
    //URL(s)
    var url: [String]?
    
    //email address
    var email: [String]?
    
    //which page of the app the information should appear on
    var page: [String]?
    
    //Which section of the page the information should appear under
    var section: [String]?
    
    //Description of the organization (or instructions for how to contact them)
    var description: String?
}
