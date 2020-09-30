import os

from cs50 import SQL
from flask import Flask, flash, jsonify, redirect, render_template, request, session
from flask_session import Session
from tempfile import mkdtemp
from werkzeug.exceptions import default_exceptions, HTTPException, InternalServerError
from werkzeug.security import check_password_hash, generate_password_hash
from datetime import datetime

from helpers import apology, login_required, lookup, usd

# Configure application
app = Flask(__name__)

# Ensure templates are auto-reloaded
app.config["TEMPLATES_AUTO_RELOAD"] = True

# Ensure responses aren't cached
@app.after_request
def after_request(response):
    response.headers["Cache-Control"] = "no-cache, no-store, must-revalidate"
    response.headers["Expires"] = 0
    response.headers["Pragma"] = "no-cache"
    return response

# Custom filter
app.jinja_env.filters["usd"] = usd

# Configure session to use filesystem (instead of signed cookies)
app.config["SESSION_FILE_DIR"] = mkdtemp()
app.config["SESSION_PERMANENT"] = False
app.config["SESSION_TYPE"] = "filesystem"
Session(app)

# Configure CS50 Library to use SQLite database
db = SQL("sqlite:///finance.db")

# Make sure API key is set
if not os.environ.get("API_KEY"):
    raise RuntimeError("API_KEY not set")


@app.route("/")
@login_required
def index():
    """Show portfolio of stocks"""
    cash = db.execute("SELECT cash FROM users WHERE id = :num", num = session["user_id"])[0]["cash"]
    data = [{'stock': 'Cash', 'shares': '--', 'price': '--', 'value': cash}]
    rows = db.execute("SELECT * FROM shares WHERE user_id = :num AND shares > 0", num = session["user_id"])
    for row in rows:
        stock = row['symbol']
        shares = row['shares']
        price = lookup(stock)['price']
        value = shares * price
        data.append({'stock': stock, 'shares': shares, 'price': price, 'value': value})
    total = 0
    for point in data:
        total += point['value']
    return render_template("index.html", total = total, data = data)


@app.route("/buy", methods=["GET", "POST"])
@login_required
def buy():
    # User reached route via POST
    if request.method == "POST":
        # Ensure stock symbol was submitted
        if not lookup(request.form.get("symbol")):
            return apology("must provide valid symbol", 403)

        if not request.form.get("shares"):
            return apology("must provide valid number of shares", 403)

        # Ensure positive number of shares
        if not int(request.form.get("shares")) > 0:
            return apology("must provide valid number of shares", 403)

        # check if user has enough cash to purchase stocks
        shares = request.form.get("shares")
        price = lookup(request.form.get("symbol"))['price']
        rows = db.execute("SELECT cash FROM users WHERE id = :num", num = session["user_id"])
        cash = rows[0]["cash"]

        if (price * float(shares)) > cash:
            return apology("you can't afford that many shares", 403)

        #else extract data from dictionary and post to webpage
        else:
            db.execute("INSERT INTO history (user_id, symbol, cost, shares, time, action) VALUES (:user_id, :symbol, :cost, :shares, CURRENT_TIMESTAMP, 'Bought')",
            user_id = session["user_id"],
            symbol = lookup(request.form.get("symbol"))['symbol'],
            cost = price,
            shares = shares)
            db.execute("UPDATE users SET cash = (cash - (:price * :shares)) WHERE id = :num", num = session["user_id"],
            price = price, shares = float(shares))
            if len(db.execute("SELECT * FROM shares WHERE user_id = :num AND symbol = :symbol", num = session["user_id"], symbol = lookup(request.form.get("symbol"))['symbol'])) == 0:
                db.execute("INSERT INTO shares (user_id, symbol, shares) VALUES(:num, :symbol, :shares)",
                shares = shares, num = session["user_id"], symbol = lookup(request.form.get("symbol"))['symbol'])
            else:
                db.execute("UPDATE shares SET shares = (shares + :shares) WHERE user_id = :num AND symbol = :symbol",
                shares = shares, num = session["user_id"], symbol = lookup(request.form.get("symbol"))['symbol'])
            return render_template("buy.html")

    # User reached route via GET
    else:
        return render_template("buy.html")


@app.route("/history")
@login_required
def history():
    """Show history of transactions"""
    data = []
    for row in db.execute("SELECT * FROM history WHERE user_id = :num", num = session["user_id"]):
        data.append(row)
    return render_template("history.html", data = data)

@app.route("/password", methods=["GET", "POST"])
@login_required
def password():
    """Change user's password"""
    if request.method == "POST":

        # Ensure password was submitted
        if not request.form.get("password"):
            return apology("must enter current password", 403)

        # Query database for user data
        rows = db.execute("SELECT * FROM users WHERE id = :num", num=session["user_id"])

        # Ensure current password is correct
        if not check_password_hash(rows[0]["hash"], request.form.get("password")):
            return apology("invalid password", 403)

        # Ensure new password was submitted
        elif not request.form.get("newpassword"):
            return apology("must provide a new password", 403)

        # Ensure confirmation was submitted
        elif not request.form.get("confirmation"):
            return apology("must confirm password", 403)

        # Ensure password matches confirmation
        elif not request.form.get("newpassword") == request.form.get("confirmation"):
            return apology("new passwords do not match", 403)

        # Ensure new password is not the same as the old password
        if check_password_hash(rows[0]["hash"], request.form.get("newpassword")):
            return apology("new password must be different", 403)

        #Edit user in users table
        else:
            db.execute("UPDATE users SET hash = :password WHERE id = :num",
                          password = generate_password_hash(request.form.get("newpassword"), method='pbkdf2:sha256', salt_length=8),
                          num = session["user_id"])
        return redirect("/")

    else:
        return render_template("password.html")


@app.route("/login", methods=["GET", "POST"])
def login():
    """Log user in"""

    # Forget any user_id
    session.clear()

    # User reached route via POST (as by submitting a form via POST)
    if request.method == "POST":

        # Ensure username was submitted
        if not request.form.get("username"):
            return apology("must provide username", 403)

        # Ensure password was submitted
        elif not request.form.get("password"):
            return apology("must provide password", 403)

        # Query database for username
        rows = db.execute("SELECT * FROM users WHERE username = :username",
                          username=request.form.get("username"))

        # Ensure username exists and password is correct
        if len(rows) != 1 or not check_password_hash(rows[0]["hash"], request.form.get("password")):
            return apology("invalid username and/or password", 403)

        # Remember which user has logged in
        session["user_id"] = rows[0]["id"]

        # Redirect user to home page
        return redirect("/")

    # User reached route via GET (as by clicking a link or via redirect)
    else:
        return render_template("login.html")


@app.route("/logout")
def logout():
    """Log user out"""

    # Forget any user_id
    session.clear()

    # Redirect user to login form
    return redirect("/")


@app.route("/quote", methods=["GET", "POST"])
@login_required
def quote():

    # User reached route via POST
    if request.method == "POST":
        # Ensure stock symbol was submitted
        if not lookup(request.form.get("symbol")):
            return apology("must provide valid symbol", 403)

        #else extract data from dictionary and post to webpage
        else:
            session["data"].append(lookup(request.form.get("symbol")))
            return render_template("quote.html", data = session["data"])

    # User reached route via GET
    else:
        session["data"] = []
        return render_template("quote.html", data = session["data"])

@app.route("/register", methods=["GET", "POST"])
def register():

    # User reached route via POST (as by submitting a form via POST)
    if request.method == "POST":

        # Ensure username was submitted
        if not request.form.get("username"):
            return apology("must provide username", 403)

        # Ensure password was submitted
        elif not request.form.get("password"):
            return apology("must provide password", 403)

        # Ensure confirmation was submitted
        elif not request.form.get("confirmation"):
            return apology("must confirm password", 403)

        # Ensure password matches confirmation
        elif not request.form.get("password") == request.form.get("confirmation"):
            return apology("passwords do not match", 403)

        #Create user in users table
        db.execute("INSERT INTO users (username, hash) VALUES (:username, :password)",
                          username=request.form.get("username"),
                          password = generate_password_hash(request.form.get("password"), method='pbkdf2:sha256', salt_length=8))
        return render_template("login.html")

    # User reached route via GET (as by clicking a link or via redirect)
    else:
        return render_template("register.html")


@app.route("/sell", methods=["GET", "POST"])
@login_required
def sell():
    """Sell shares of stock"""

    # User reached route via POST
    if request.method == "POST":
        # Ensure stock symbol was submitted
        if not lookup(request.form.get("symbol")):
            return apology("must provide valid symbol", 403)

        if not request.form.get("shares"):
            return apology("must provide valid number of shares", 403)

        # Ensure positive number of shares
        if not int(request.form.get("shares")) > 0:
            return apology("must provide valid number of shares", 403)

        # check if user has enough shares to sell that many
        shares = request.form.get("shares")
        price = lookup(request.form.get("symbol"))['price']
        rows = db.execute("SELECT shares FROM shares WHERE user_id = :num AND symbol = :symbol",
        num = session["user_id"], symbol = lookup(request.form.get("symbol"))['symbol'])
        owned = rows[0]["shares"]

        if int(shares) > owned:
            return apology("you don't have that many shares", 403)

        #else extract data from dictionary and post to webpage
        else:
            db.execute("INSERT INTO history (user_id, symbol, cost, shares, time, action) VALUES (:user_id, :symbol, :cost, :shares, CURRENT_TIMESTAMP, 'Sold')",
            user_id = session["user_id"],
            symbol = lookup(request.form.get("symbol"))['symbol'],
            cost = price,
            shares = shares)
            db.execute("UPDATE users SET cash = (cash + (:price * :shares)) WHERE id = :num", num = session["user_id"],
            price = price, shares = float(shares))

            db.execute("UPDATE shares SET shares = (shares - :shares) WHERE user_id = :num AND symbol = :symbol",
            shares = shares, num = session["user_id"], symbol = lookup(request.form.get("symbol"))['symbol'])

            data = []
            for row in db.execute("SELECT * FROM shares WHERE user_id = :num AND shares > 0", num = session["user_id"]):
                data.append(row['symbol'])
            return render_template("sell.html", data = data)

    # User reached route via GET
    else:
        data = []
        for row in db.execute("SELECT * FROM shares WHERE user_id = :num AND shares > 0", num = session["user_id"]):
            data.append(row['symbol'])
        return render_template("sell.html", data = data)


def errorhandler(e):
    """Handle error"""
    if not isinstance(e, HTTPException):
        e = InternalServerError()
    return apology(e.name, e.code)


# Listen for errors
for code in default_exceptions:
    app.errorhandler(code)(errorhandler)
