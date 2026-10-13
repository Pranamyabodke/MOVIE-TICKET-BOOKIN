/**
 * CineShow - Movie Ticket Booking Frontend App
 * Communicates with pure Java HTTP REST API backend
 */

const API_BASE = ''; // Same origin (http://localhost:8080)

const app = {
    // Application State
    currentUser: null,
    movies: [],
    currentMovie: null,
    currentShowtime: null,
    selectedDate: null,
    selectedSeats: new Set(),
    selectedCategory: 'Prime',
    seatUnitPrice: 240,
    currentPaymentMethod: 'UPI (QR Code)',

    // ----------------------------------------------------
    // INITIALIZATION
    // ----------------------------------------------------
    init() {
        console.log('[CineShow] Initializing Movie Ticket Booking System...');
        
        // 1. Restore logged-in user from localStorage
        const savedUser = localStorage.getItem('cineshow_user');
        if (savedUser) {
            try {
                this.currentUser = JSON.parse(savedUser);
            } catch (e) {
                localStorage.removeItem('cineshow_user');
            }
        }
        this.updateAuthUI();

        // 2. Fetch and render movies
        this.fetchMovies();

        // 3. Setup event listeners
        this.setupEventListeners();
    },

    setupEventListeners() {
        // Search Input with Debounce
        const searchInput = document.getElementById('searchInput');
        const searchClearBtn = document.getElementById('searchClearBtn');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                const val = e.target.value.trim();
                if (searchClearBtn) {
                    searchClearBtn.style.display = val ? 'block' : 'none';
                }
                this.filterMovies();
            });
        }
        if (searchClearBtn) {
            searchClearBtn.addEventListener('click', () => {
                searchInput.value = '';
                searchClearBtn.style.display = 'none';
                this.filterMovies();
            });
        }

        // Genre Filter Pills
        const pills = document.querySelectorAll('#genrePills .pill');
        pills.forEach(pill => {
            pill.addEventListener('click', () => {
                pills.forEach(p => p.classList.remove('active'));
                pill.classList.add('active');
                this.filterMovies();
            });
        });

        // Language Filter Dropdown
        const langSelect = document.getElementById('languageSelect');
        if (langSelect) {
            langSelect.addEventListener('change', () => this.filterMovies());
        }

        // Project Info Button
        const btnProjectInfo = document.getElementById('btnProjectInfo');
        if (btnProjectInfo) {
            btnProjectInfo.addEventListener('click', () => this.openModal('projectModal'));
        }

        // Close modals on clicking overlay background
        document.querySelectorAll('.modal-overlay').forEach(overlay => {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) {
                    overlay.classList.remove('active');
                }
            });
        });

        // Card number auto-formatting
        const cardInput = document.getElementById('cardNumberInput');
        if (cardInput) {
            cardInput.addEventListener('input', (e) => {
                let val = e.target.value.replace(/\D/g, '').substring(0, 16);
                let formatted = val.match(/.{1,4}/g)?.join(' ') || val;
                e.target.value = formatted;
            });
        }
    },

    // ----------------------------------------------------
    // NAVIGATION & VIEW SWITCHING
    // ----------------------------------------------------
    showHomeView() {
        this.switchView('homeView');
        this.currentMovie = null;
        this.currentShowtime = null;
        this.selectedSeats.clear();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    showShowtimesView() {
        this.switchView('showtimesView');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    showSeatsView() {
        this.switchView('seatsView');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    showBookingsView() {
        if (!this.currentUser) {
            this.showToast('Please sign in to view your bookings', 'error');
            this.openAuthModal('login');
            return;
        }
        this.switchView('bookingsView');
        this.fetchUserBookings();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    backToShowtimes() {
        if (this.currentMovie) {
            this.showShowtimesView();
        } else {
            this.showHomeView();
        }
    },

    switchView(viewId) {
        document.querySelectorAll('.view-section').forEach(view => {
            view.classList.remove('active');
        });
        const target = document.getElementById(viewId);
        if (target) target.classList.add('active');
    },

    // ----------------------------------------------------
    // MOVIES CATALOG
    // ----------------------------------------------------
    async fetchMovies() {
        const grid = document.getElementById('movieGrid');
        try {
            const res = await fetch(`${API_BASE}/api/movies`);
            if (!res.ok) throw new Error('Failed to load movies');
            this.movies = await res.json();
            this.renderMovies(this.movies);
        } catch (err) {
            console.error(err);
            if (grid) {
                grid.innerHTML = `
                    <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #ef4444;">
                        <i class="fa-solid fa-triangle-exclamation fa-2x"></i>
                        <p style="margin-top: 10px;">Failed to connect to Java server. Please ensure the server is running on port 8080.</p>
                    </div>`;
            }
        }
    },

    filterMovies() {
        const searchVal = document.getElementById('searchInput')?.value.toLowerCase().trim() || '';
        const activeGenre = document.querySelector('#genrePills .pill.active')?.getAttribute('data-genre') || 'All';
        const activeLang = document.getElementById('languageSelect')?.value || 'All';

        const filtered = this.movies.filter(movie => {
            const matchesSearch = !searchVal || 
                movie.title.toLowerCase().includes(searchVal) || 
                movie.genre.toLowerCase().includes(searchVal) ||
                movie.cast.toLowerCase().includes(searchVal);

            const matchesGenre = activeGenre === 'All' || movie.genre.toLowerCase().includes(activeGenre.toLowerCase());
            const matchesLang = activeLang === 'All' || movie.language.toLowerCase().includes(activeLang.toLowerCase());

            return matchesSearch && matchesGenre && matchesLang;
        });

        this.renderMovies(filtered);
    },

    renderMovies(movieList) {
        const grid = document.getElementById('movieGrid');
        const countText = document.getElementById('movieCountText');
        if (countText) {
            countText.textContent = `${movieList.length} Movie${movieList.length === 1 ? '' : 's'} Available`;
        }

        if (!grid) return;

        if (movieList.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 60px 20px; color: #9ca3af;">
                    <i class="fa-solid fa-film fa-3x" style="opacity: 0.3; margin-bottom: 12px;"></i>
                    <p style="font-size: 1.1rem; font-weight: 500;">No movies found matching your filters</p>
                    <p style="font-size: 0.85rem; color: #6b7280; margin-top: 4px;">Try searching for different terms or reset your filters.</p>
                </div>`;
            return;
        }

        grid.innerHTML = movieList.map(movie => `
            <div class="movie-card" onclick="app.selectMovie('${movie.id}')">
                <div class="movie-poster-box">
                    <img src="${movie.posterUrl}" alt="${movie.title}" onerror="this.src='https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600&fit=crop'">
                    <div class="movie-rating-overlay">
                        <i class="fa-solid fa-star"></i>
                        <span>${movie.rating.toFixed(1)}/10</span>
                        <span style="font-size: 0.72rem; color: #d1d5db; margin-left: auto;">${movie.votes}</span>
                    </div>
                </div>
                <div class="movie-info-box">
                    <h3 class="movie-title" title="${movie.title}">${movie.title}</h3>
                    <p class="movie-genre">${movie.genre}</p>
                    <div class="movie-lang-censor">
                        <span class="badge-tag">${movie.language.split(',')[0]}</span>
                        <span class="badge-tag">${movie.censorRating}</span>
                        <span class="badge-tag" style="margin-left: auto;">${movie.duration}</span>
                    </div>
                    <button class="btn-primary" onclick="event.stopPropagation(); app.selectMovie('${movie.id}')">
                        <i class="fa-solid fa-ticket"></i> Book Tickets
                    </button>
                </div>
            </div>
        `).join('');
    },

    // ----------------------------------------------------
    // MOVIE SHOWTIMES & THEATERS
    // ----------------------------------------------------
    async selectMovie(movieId) {
        this.currentMovie = this.movies.find(m => m.id === movieId);
        if (!this.currentMovie) {
            // fetch single movie
            try {
                const res = await fetch(`${API_BASE}/api/movies/${movieId}`);
                this.currentMovie = await res.json();
            } catch (err) {
                this.showToast('Could not load movie details', 'error');
                return;
            }
        }

        this.renderMovieBanner(this.currentMovie);
        this.showShowtimesView();
        this.setupDatePills();
    },

    renderMovieBanner(movie) {
        const banner = document.getElementById('selectedMovieBanner');
        if (!banner) return;

        banner.innerHTML = `
            <img class="banner-poster" src="${movie.posterUrl}" alt="${movie.title}">
            <div class="banner-details">
                <h1 class="banner-title">${movie.title}</h1>
                <div class="banner-meta">
                    <span class="rating-badge"><i class="fa-solid fa-star"></i> ${movie.rating.toFixed(1)}/10 (${movie.votes})</span>
                    <span>•</span>
                    <span>${movie.language}</span>
                    <span>•</span>
                    <span>${movie.duration}</span>
                    <span>•</span>
                    <span class="badge-tag">${movie.censorRating}</span>
                </div>
                <p class="banner-desc">${movie.description}</p>
                <p class="banner-cast"><strong>Starring:</strong> ${movie.cast}</p>
            </div>
        `;
    },

    setupDatePills() {
        const container = document.getElementById('datePills');
        if (!container) return;

        const dates = [];
        const today = new Date();
        const days = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];

        // Generate rolling 7 days (Today, Tomorrow, and next 5 days)
        for (let i = 0; i < 7; i++) {
            const d = new Date(today);
            d.setDate(today.getDate() + i);

            // Format YYYY-MM-DD
            const yyyy = d.getFullYear();
            const mm = String(d.getMonth() + 1).padStart(2, '0');
            const dd = String(d.getDate()).padStart(2, '0');
            const dateStr = `${yyyy}-${mm}-${dd}`;

            dates.push({
                dateStr: dateStr,
                dayName: i === 0 ? 'TODAY' : (i === 1 ? 'TOMORROW' : days[d.getDay()]),
                dayNum: d.getDate(),
                month: d.toLocaleString('default', { month: 'short' })
            });
        }

        this.selectedDate = dates[0].dateStr;

        container.innerHTML = dates.map((d, index) => `
            <button class="date-btn ${index === 0 ? 'active' : ''}" data-date="${d.dateStr}" onclick="app.changeShowDate('${d.dateStr}', this)">
                <span class="date-day">${d.dayName}</span>
                <span class="date-num">${d.dayNum} ${d.month}</span>
            </button>
        `).join('');

        this.fetchShowtimesForDate(this.selectedDate);
    },

    changeShowDate(dateStr, btnElement) {
        this.selectedDate = dateStr;
        document.querySelectorAll('#datePills .date-btn').forEach(b => b.classList.remove('active'));
        if (btnElement) btnElement.classList.add('active');
        this.fetchShowtimesForDate(dateStr);
    },

    async fetchShowtimesForDate(dateStr) {
        const listContainer = document.getElementById('theatersList');
        if (!listContainer || !this.currentMovie) return;

        listContainer.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #9ca3af;">
                <i class="fa-solid fa-circle-notch fa-spin fa-2x"></i>
                <p style="margin-top: 10px;">Finding available cinema shows...</p>
            </div>`;

        try {
            const res = await fetch(`${API_BASE}/api/movies/${this.currentMovie.id}/showtimes?date=${dateStr}`);
            const showtimes = await res.json();

            if (showtimes.length === 0) {
                listContainer.innerHTML = `
                    <div style="text-align: center; padding: 50px; background: #181a24; border-radius: 12px;">
                        <p style="font-size: 1.1rem; color: #9ca3af;">No showtimes available for this date.</p>
                    </div>`;
                return;
            }

            // Group showtimes by theater
            const theaterMap = {};
            showtimes.forEach(st => {
                if (!theaterMap[st.theaterName]) {
                    theaterMap[st.theaterName] = {
                        name: st.theaterName,
                        location: st.theaterLocation,
                        screen: st.screen,
                        shows: []
                    };
                }
                theaterMap[st.theaterName].shows.push(st);
            });

            listContainer.innerHTML = Object.values(theaterMap).map(theater => `
                <div class="theater-card">
                    <div class="theater-info">
                        <h3 class="theater-name"><i class="fa-solid fa-film" style="color: #f84464; margin-right: 6px;"></i> ${theater.name}</h3>
                        <p class="theater-loc"><i class="fa-solid fa-location-dot"></i> ${theater.location}</p>
                        <div class="theater-amenities">
                            <span class="amenity-chip"><i class="fa-solid fa-mobile-screen"></i> M-Ticket</span>
                            <span class="amenity-chip"><i class="fa-solid fa-utensils"></i> F&B Available</span>
                            <span class="amenity-chip"><i class="fa-solid fa-couch"></i> Recliner</span>
                        </div>
                    </div>
                    <div class="showtimes-slots">
                        ${theater.shows.map(show => `
                            <button class="slot-btn" onclick="app.selectShowtime('${show.id}')">
                                <span class="slot-time">${show.time}</span>
                                <span class="slot-screen">${show.screen.split('(')[0].trim()}</span>
                            </button>
                        `).join('')}
                    </div>
                </div>
            `).join('');

        } catch (err) {
            console.error(err);
            listContainer.innerHTML = `<p style="color: #ef4444; text-align: center;">Error loading showtimes.</p>`;
        }
    },

    // ----------------------------------------------------
    // SEAT SELECTION
    // ----------------------------------------------------
    async selectShowtime(showtimeId) {
        try {
            const res = await fetch(`${API_BASE}/api/showtimes/${showtimeId}`);
            this.currentShowtime = await res.json();
            this.selectedSeats.clear();
            this.renderSeatLayout(this.currentShowtime);
            this.showSeatsView();
        } catch (err) {
            this.showToast('Could not load showtime details', 'error');
        }
    },

    renderSeatLayout(showtime) {
        // Summary in header
        const summary = document.getElementById('seatShowSummary');
        if (summary && this.currentMovie) {
            summary.innerHTML = `
                <h3>${this.currentMovie.title} (${this.currentMovie.language})</h3>
                <p>${showtime.theaterName} | ${showtime.screen} | <strong>${showtime.date} at ${showtime.time}</strong></p>
            `;
        }

        const grid = document.getElementById('seatGrid');
        if (!grid) return;

        const bookedSet = new Set(showtime.bookedSeats || []);

        // Define Tiers:
        // Platinum Recliner: Rows A, B (₹380)
        // Gold Prime: Rows C, D, E (₹240)
        // Silver Classic: Rows F, G, H (₹160)
        const tiers = [
            { name: 'Platinum Recliner', price: showtime.priceRecliner, rows: ['A', 'B'] },
            { name: 'Gold Prime', price: showtime.pricePrime, rows: ['C', 'D', 'E'] },
            { name: 'Silver Classic', price: showtime.priceClassic, rows: ['F', 'G', 'H'] }
        ];

        let html = '';

        tiers.forEach(tier => {
            html += `
                <div class="seat-tier-section">
                    <div class="tier-title">
                        <span>${tier.name}</span> • <strong>₹${tier.price}</strong>
                    </div>
                    <div class="seat-rows">
            `;

            tier.rows.forEach(rowLetter => {
                html += `
                    <div class="seat-row">
                        <span class="row-label">${rowLetter}</span>
                        <div class="row-seats">
                `;

                for (let num = 1; num <= 10; num++) {
                    const seatId = `${rowLetter}${num}`;
                    const isBooked = bookedSet.has(seatId);

                    // Add an aisle space between seat 5 and 6
                    if (num === 6) {
                        html += `<div class="seat-aisle"></div>`;
                    }

                    html += `
                        <div class="seat ${isBooked ? 'booked' : ''}" 
                             data-seat="${seatId}" 
                             data-tier="${tier.name}" 
                             data-price="${tier.price}"
                             onclick="app.toggleSeat('${seatId}', '${tier.name}', ${tier.price}, this)">
                            ${num}
                        </div>
                    `;
                }

                html += `
                        </div>
                        <span class="row-label">${rowLetter}</span>
                    </div>
                `;
            });

            html += `
                    </div>
                </div>
            `;
        });

        grid.innerHTML = html;
        this.updateCheckoutBar();
    },

    toggleSeat(seatId, tierName, price, seatElem) {
        if (seatElem.classList.contains('booked')) {
            this.showToast(`Seat ${seatId} is already booked!`, 'error');
            return;
        }

        if (this.selectedSeats.has(seatId)) {
            this.selectedSeats.delete(seatId);
            seatElem.classList.remove('selected');
        } else {
            // Maximum 8 seats per booking
            if (this.selectedSeats.size >= 8) {
                this.showToast('You can select a maximum of 8 seats per booking.', 'error');
                return;
            }
            this.selectedSeats.add(seatId);
            seatElem.classList.add('selected');
            this.selectedCategory = tierName;
            this.seatUnitPrice = price;
        }

        this.updateCheckoutBar();
    },

    updateCheckoutBar() {
        const badgesContainer = document.getElementById('selectedSeatsBadges');
        const priceBreakdown = document.getElementById('priceBreakdown');
        const lblBaseFare = document.getElementById('lblBaseFare');
        const lblGst = document.getElementById('lblGst');
        const lblTotalPrice = document.getElementById('lblTotalPrice');
        const btnPay = document.getElementById('btnProceedToPay');

        const seatCount = this.selectedSeats.size;

        if (seatCount === 0) {
            if (badgesContainer) badgesContainer.innerHTML = `<span class="no-selection-msg">No seats selected yet. Click on available seats to choose.</span>`;
            if (priceBreakdown) priceBreakdown.style.display = 'none';
            if (lblTotalPrice) lblTotalPrice.textContent = '₹0';
            if (btnPay) btnPay.disabled = true;
            return;
        }

        // Calculate pricing
        const sortedSeats = Array.from(this.selectedSeats).sort();
        let baseFare = 0;

        // Sum prices based on each selected seat
        sortedSeats.forEach(seatId => {
            const row = seatId.charAt(0);
            if (row === 'A' || row === 'B') {
                baseFare += this.currentShowtime.priceRecliner;
            } else if (row === 'C' || row === 'D' || row === 'E') {
                baseFare += this.currentShowtime.pricePrime;
            } else {
                baseFare += this.currentShowtime.priceClassic;
            }
        });

        const gst = Math.round(baseFare * 0.18 * 100) / 100;
        const total = Math.round((baseFare + gst) * 100) / 100;

        if (badgesContainer) {
            badgesContainer.innerHTML = sortedSeats.map(s => `
                <span class="seat-pill-badge">${s}</span>
            `).join('');
        }

        if (priceBreakdown) priceBreakdown.style.display = 'flex';
        if (lblBaseFare) lblBaseFare.textContent = `₹${baseFare.toFixed(2)}`;
        if (lblGst) lblGst.textContent = `₹${gst.toFixed(2)}`;
        if (lblTotalPrice) lblTotalPrice.textContent = `₹${total.toFixed(2)}`;
        if (btnPay) btnPay.disabled = false;
    },

    // ----------------------------------------------------
    // PAYMENT PROCESS (DUMMY GATEWAY)
    // ----------------------------------------------------
    proceedToPayment() {
        if (this.selectedSeats.size === 0) {
            this.showToast('Please select at least 1 seat to proceed.', 'error');
            return;
        }

        // Check authentication
        if (!this.currentUser) {
            this.showToast('Please sign in or create an account to proceed to payment.', 'error');
            this.openAuthModal('login');
            return;
        }

        // Open Payment Modal
        this.renderPaymentSummary();
        this.openModal('paymentModal');
    },

    renderPaymentSummary() {
        const sortedSeats = Array.from(this.selectedSeats).sort();
        let baseFare = 0;
        sortedSeats.forEach(seatId => {
            const row = seatId.charAt(0);
            if (row === 'A' || row === 'B') baseFare += this.currentShowtime.priceRecliner;
            else if (row === 'C' || row === 'D' || row === 'E') baseFare += this.currentShowtime.pricePrime;
            else baseFare += this.currentShowtime.priceClassic;
        });

        const gst = Math.round(baseFare * 0.18 * 100) / 100;
        const total = Math.round((baseFare + gst) * 100) / 100;

        const infoBox = document.getElementById('payMovieInfo');
        if (infoBox && this.currentMovie && this.currentShowtime) {
            infoBox.innerHTML = `
                <div style="display: flex; gap: 12px; align-items: center; margin-bottom: 12px;">
                    <img src="${this.currentMovie.posterUrl}" style="width: 50px; height: 70px; object-fit: cover; border-radius: 4px;">
                    <div>
                        <strong style="display: block; font-size: 0.95rem; color: #fff;">${this.currentMovie.title}</strong>
                        <span style="font-size: 0.78rem; color: #9ca3af;">${this.currentShowtime.theaterName}</span>
                        <div style="font-size: 0.75rem; color: #10b981; margin-top: 2px;">
                            ${this.currentShowtime.date} at ${this.currentShowtime.time}
                        </div>
                    </div>
                </div>
                <div style="background: #1e2230; padding: 8px 12px; border-radius: 6px; font-size: 0.8rem; color: #e5e7eb;">
                    Seats: <strong>${sortedSeats.join(', ')}</strong> (${sortedSeats.length} Tickets)
                </div>
            `;
        }

        document.getElementById('paySeatCount').textContent = sortedSeats.length;
        document.getElementById('payBaseFare').textContent = `₹${baseFare.toFixed(2)}`;
        document.getElementById('payGst').textContent = `₹${gst.toFixed(2)}`;
        document.getElementById('payTotalAmount').textContent = `₹${total.toFixed(2)}`;
        document.getElementById('btnPayAmount').textContent = `₹${total.toFixed(2)}`;
    },

    switchPayTab(tabName) {
        document.querySelectorAll('.pay-tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.pay-tab-content').forEach(c => c.classList.remove('active'));

        const targetTab = document.querySelector(`.pay-tab[data-tab="${tabName}"]`);
        if (targetTab) targetTab.classList.add('active');

        if (tabName === 'upi') {
            document.getElementById('tabContentUpi')?.classList.add('active');
            this.currentPaymentMethod = 'UPI (QR Code / Scan & Pay)';
        } else if (tabName === 'card') {
            document.getElementById('tabContentCard')?.classList.add('active');
            this.currentPaymentMethod = 'Debit / Credit Card';
        } else if (tabName === 'netbanking') {
            document.getElementById('tabContentNetbanking')?.classList.add('active');
            this.currentPaymentMethod = 'Net Banking (HDFC)';
        }
    },

    selectBank(btnElem, bankName) {
        document.querySelectorAll('.bank-btn').forEach(b => b.classList.remove('active'));
        if (btnElem) btnElem.classList.add('active');
        this.currentPaymentMethod = `Net Banking (${bankName})`;
    },

    async processPayment() {
        const overlay = document.getElementById('paymentProcessingOverlay');
        const title = document.getElementById('processingStatusTitle');
        const sub = document.getElementById('processingStatusSub');

        if (overlay) overlay.style.display = 'flex';

        // Simulated steps
        if (title) title.textContent = 'Securing Connection...';
        if (sub) sub.textContent = 'Connecting with payment gateway simulated bank API';

        await new Promise(r => setTimeout(r, 900));

        if (title) title.textContent = 'Authorizing Transaction...';
        if (sub) sub.textContent = `Verifying payment via ${this.currentPaymentMethod}`;

        await new Promise(r => setTimeout(r, 900));

        // Send booking to Java Backend
        const sortedSeats = Array.from(this.selectedSeats).sort();
        let baseFare = 0;
        sortedSeats.forEach(seatId => {
            const row = seatId.charAt(0);
            if (row === 'A' || row === 'B') baseFare += this.currentShowtime.priceRecliner;
            else if (row === 'C' || row === 'D' || row === 'E') baseFare += this.currentShowtime.pricePrime;
            else baseFare += this.currentShowtime.priceClassic;
        });
        const gst = Math.round(baseFare * 0.18 * 100) / 100;
        const total = Math.round((baseFare + gst) * 100) / 100;

        const bookingPayload = {
            userId: this.currentUser.id,
            showtimeId: this.currentShowtime.id,
            seats: sortedSeats,
            seatCategory: this.selectedCategory,
            baseAmount: baseFare,
            convenienceFee: gst,
            totalAmount: total,
            paymentMethod: this.currentPaymentMethod
        };

        try {
            const res = await fetch(`${API_BASE}/api/bookings`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(bookingPayload)
            });

            const data = await res.json();

            if (!res.ok) {
                if (overlay) overlay.style.display = 'none';
                this.showToast(data.error || 'Payment failed: Could not complete booking', 'error');
                // If seats are taken, refresh layout
                if (res.status === 409) {
                    this.closeModal('paymentModal');
                    this.selectShowtime(this.currentShowtime.id);
                }
                return;
            }

            // Payment Success animation
            if (title) {
                title.innerHTML = '<i class="fa-solid fa-circle-check" style="color: #10b981; font-size: 2.5rem; display: block; margin-bottom: 8px;"></i> Payment Approved!';
            }
            if (sub) sub.textContent = 'Generating your CineShow ticket pass...';

            await new Promise(r => setTimeout(r, 1000));

            if (overlay) overlay.style.display = 'none';
            this.closeModal('paymentModal');

            // Show Confirmation Ticket Pass Modal
            this.showTicketPass(data.booking);
            this.showToast('🎉 Ticket Booked Successfully!', 'success');

        } catch (err) {
            console.error(err);
            if (overlay) overlay.style.display = 'none';
            this.showToast('Server communication error. Please check backend connection.', 'error');
        }
    },

    // ----------------------------------------------------
    // TICKET PASS MODAL
    // ----------------------------------------------------
    showTicketPass(booking) {
        if (!booking) return;

        document.getElementById('tktPoster').src = booking.moviePoster || '';
        document.getElementById('tktTitle').textContent = booking.movieTitle;
        document.getElementById('tktLanguage').textContent = `${booking.movieLanguage} • 2D • Audi Pass`;
        document.getElementById('tktTheater').textContent = booking.theaterName;
        document.getElementById('tktScreen').textContent = booking.screen;
        document.getElementById('tktDate').textContent = booking.showDate;
        document.getElementById('tktTime').textContent = booking.showTime;
        document.getElementById('tktCategory').textContent = booking.seatCategory || 'Prime';
        document.getElementById('tktSeats').textContent = (booking.seats || []).join(', ');
        document.getElementById('tktBookingId').textContent = booking.id;
        document.getElementById('tktAmount').textContent = `₹${(booking.totalAmount || 0).toFixed(2)}`;
        document.getElementById('tktBarcode').textContent = (booking.id || '').replace(/\D/g, '') + '984712';

        const statusBadge = document.getElementById('tktStatusBadge');
        if (statusBadge) {
            statusBadge.textContent = booking.status;
            statusBadge.style.background = booking.status === 'CONFIRMED' ? '#10b981' : '#ef4444';
        }

        this.openModal('ticketModal');
    },

    // ----------------------------------------------------
    // MY BOOKINGS
    // ----------------------------------------------------
    async fetchUserBookings() {
        const grid = document.getElementById('bookingsGrid');
        if (!grid || !this.currentUser) return;

        grid.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #9ca3af;">
                <i class="fa-solid fa-circle-notch fa-spin fa-2x"></i>
                <p style="margin-top: 10px;">Loading your booked tickets...</p>
            </div>`;

        try {
            const res = await fetch(`${API_BASE}/api/bookings?userId=${this.currentUser.id}`);
            const bookings = await res.json();

            if (bookings.length === 0) {
                grid.innerHTML = `
                    <div style="text-align: center; padding: 60px 20px; background: #181a24; border-radius: 12px; border: 1px solid #2b2e40;">
                        <i class="fa-solid fa-ticket-simple fa-3x" style="opacity: 0.3; margin-bottom: 12px;"></i>
                        <h3 style="font-size: 1.2rem; color: #e5e7eb;">No Tickets Booked Yet</h3>
                        <p style="color: #9ca3af; font-size: 0.85rem; margin: 8px 0 20px;">Explore our catalog of trending movies and book your first show!</p>
                        <button class="btn-primary" onclick="app.showHomeView()"><i class="fa-solid fa-film"></i> Browse Movies</button>
                    </div>`;
                return;
            }

            grid.innerHTML = bookings.map(b => `
                <div class="booking-item-card ${b.status === 'CANCELLED' ? 'cancelled' : ''}">
                    <span class="booking-status-tag ${b.status === 'CONFIRMED' ? 'confirmed' : 'cancelled'}">
                        ${b.status}
                    </span>
                    <img class="booking-poster" src="${b.moviePoster}" alt="${b.movieTitle}">
                    <div class="booking-details">
                        <h3 class="booking-item-title">${b.movieTitle}</h3>
                        <p class="booking-item-theater"><i class="fa-solid fa-location-dot"></i> ${b.theaterName} (${b.screen})</p>
                        <div class="booking-item-info-row">
                            <span><i class="fa-regular fa-calendar"></i> <strong>${b.showDate}</strong> at <strong>${b.showTime}</strong></span>
                            <span>Seats: <strong style="color: #10b981;">${(b.seats || []).join(', ')}</strong></span>
                            <span>Total: <strong style="color: #38bdf8;">₹${(b.totalAmount || 0).toFixed(2)}</strong></span>
                        </div>
                        <div class="booking-actions">
                            <button class="btn-secondary" onclick='app.showTicketPass(${JSON.stringify(b)})'>
                                <i class="fa-solid fa-ticket"></i> View Pass
                            </button>
                            ${b.status === 'CONFIRMED' ? `
                                <button class="btn-secondary" style="color: #ef4444; border-color: rgba(239, 68, 68, 0.4);" onclick="app.cancelBooking('${b.id}')">
                                    <i class="fa-solid fa-ban"></i> Cancel Ticket
                                </button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            `).join('');

        } catch (err) {
            console.error(err);
            grid.innerHTML = `<p style="color: #ef4444; text-align: center;">Failed to load bookings.</p>`;
        }
    },

    async cancelBooking(bookingId) {
        if (!confirm('Are you sure you want to cancel this ticket? The seats will be released back to other users.')) {
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/api/bookings/${bookingId}/cancel`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId: this.currentUser.id })
            });

            const data = await res.json();
            if (res.ok) {
                this.showToast('Ticket cancelled successfully. Seats released.', 'success');
                this.fetchUserBookings();
            } else {
                this.showToast(data.error || 'Failed to cancel ticket', 'error');
            }
        } catch (err) {
            this.showToast('Error cancelling booking.', 'error');
        }
    },

    // ----------------------------------------------------
    // AUTHENTICATION
    // ----------------------------------------------------
    openAuthModal(tab = 'login') {
        this.switchAuthTab(tab);
        this.openModal('authModal');
    },

    switchAuthTab(tab) {
        const loginBtn = document.getElementById('tabLoginBtn');
        const regBtn = document.getElementById('tabRegisterBtn');
        const loginForm = document.getElementById('loginForm');
        const regForm = document.getElementById('registerForm');

        if (tab === 'login') {
            loginBtn?.classList.add('active');
            regBtn?.classList.remove('active');
            if (loginForm) loginForm.style.display = 'block';
            if (regForm) regForm.style.display = 'none';
        } else {
            regBtn?.classList.add('active');
            loginBtn?.classList.remove('active');
            if (loginForm) loginForm.style.display = 'none';
            if (regForm) regForm.style.display = 'block';
        }
    },

    async handleLogin(e) {
        e.preventDefault();
        const email = document.getElementById('loginEmail')?.value.trim();
        const password = document.getElementById('loginPassword')?.value;

        try {
            const res = await fetch(`${API_BASE}/api/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            const data = await res.json();

            if (res.ok && data.success) {
                this.currentUser = data.user;
                localStorage.setItem('cineshow_user', JSON.stringify(data.user));
                this.updateAuthUI();
                this.closeModal('authModal');
                this.showToast(`Welcome back, ${data.user.name}!`, 'success');
            } else {
                this.showToast(data.error || 'Invalid email or password', 'error');
            }
        } catch (err) {
            this.showToast('Could not connect to authentication server.', 'error');
        }
    },

    async handleRegister(e) {
        e.preventDefault();
        const name = document.getElementById('regName')?.value.trim();
        const email = document.getElementById('regEmail')?.value.trim();
        const phone = document.getElementById('regPhone')?.value.trim();
        const password = document.getElementById('regPassword')?.value;

        try {
            const res = await fetch(`${API_BASE}/api/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, phone, password })
            });
            const data = await res.json();

            if (res.ok && data.success) {
                this.currentUser = data.user;
                localStorage.setItem('cineshow_user', JSON.stringify(data.user));
                this.updateAuthUI();
                this.closeModal('authModal');
                this.showToast(`Account created! Welcome, ${data.user.name}!`, 'success');
            } else {
                this.showToast(data.error || 'Registration failed', 'error');
            }
        } catch (err) {
            this.showToast('Could not connect to authentication server.', 'error');
        }
    },

    logout() {
        this.currentUser = null;
        localStorage.removeItem('cineshow_user');
        this.updateAuthUI();
        this.showToast('Logged out successfully.', 'success');
        this.showHomeView();
    },

    updateAuthUI() {
        const btnSignIn = document.getElementById('btnSignIn');
        const userDropdown = document.getElementById('userProfileDropdown');
        const navUserName = document.getElementById('navUserName');
        const navUserAvatar = document.getElementById('navUserAvatar');
        const dropdownUserName = document.getElementById('dropdownUserName');
        const dropdownUserEmail = document.getElementById('dropdownUserEmail');

        if (this.currentUser) {
            if (btnSignIn) btnSignIn.style.display = 'none';
            if (userDropdown) userDropdown.style.display = 'block';

            const initials = this.currentUser.name.charAt(0).toUpperCase();
            if (navUserAvatar) navUserAvatar.textContent = initials;
            if (navUserName) navUserName.textContent = this.currentUser.name.split(' ')[0];
            if (dropdownUserName) dropdownUserName.textContent = this.currentUser.name;
            if (dropdownUserEmail) dropdownUserEmail.textContent = this.currentUser.email;
        } else {
            if (btnSignIn) btnSignIn.style.display = 'inline-flex';
            if (userDropdown) userDropdown.style.display = 'none';
        }
    },

    toggleUserMenu() {
        const panel = document.getElementById('userDropdownPanel');
        if (panel) panel.classList.toggle('show');
    },

    // ----------------------------------------------------
    // MODAL UTILITIES
    // ----------------------------------------------------
    openModal(modalId) {
        const m = document.getElementById(modalId);
        if (m) m.classList.add('active');
    },

    closeModal(modalId) {
        const m = document.getElementById(modalId);
        if (m) m.classList.remove('active');
    },

    showToast(message, type = 'info') {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        const icon = type === 'success' ? 'fa-circle-check' : (type === 'error' ? 'fa-circle-xmark' : 'fa-circle-info');
        toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${message}</span>`;

        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    }
};

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    app.init();
});

