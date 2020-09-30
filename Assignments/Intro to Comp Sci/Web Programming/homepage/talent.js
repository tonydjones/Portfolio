function rainbow()
{
    let x = document.getElementById("talent");
        if (x.style.color === "red")
        {
            x.style.color = "gray";
        }
        else if (x.style.color === "gray")
        {
            x.style.color = "maroon";
        }
        else if (x.style.color === "maroon")
        {
            x.style.color = "fuchsia";
        }
        else if (x.style.color === "fuchsia")
        {
            x.style.color = "black";
        }
        else if (x.style.color === "black")
        {
            x.style.color = "purple";
        }
        else
        {
            x.style.color = "red";
        }
    }
window.setInterval(rainbow, 167);